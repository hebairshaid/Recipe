#!/usr/bin/env python3
"""
Generates recipe_assistant.tflite + ingredients_vocab.txt for the Recipe app.

Model: multi-label ingredient classifier
  Input:  [1, VOCAB_SIZE] float32 — keyword presence features from user text
  Output: [1, VOCAB_SIZE] float32 — sigmoid probabilities per ingredient

Dataset: synthetic phrases like "chicken and rice", "I have tomato, onion"
Train with TensorFlow, export to TFLite.

Usage:
  pip install tensorflow
  python scripts/generate_recipe_assistant_model.py
"""

from __future__ import annotations

import random
from pathlib import Path

import numpy as np

try:
    import tensorflow as tf
except ImportError as e:
    raise SystemExit(
        "TensorFlow is required. Install with: pip install tensorflow"
    ) from e

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app" / "src" / "main" / "assets"

INGREDIENTS = [
    "chicken", "beef", "pork", "lamb", "fish", "salmon", "tuna", "shrimp", "egg",
    "rice", "pasta", "noodle", "bread", "flour", "potato", "tomato", "onion",
    "garlic", "carrot", "pepper", "mushroom", "spinach", "broccoli", "corn",
    "bean", "lentil", "chickpea", "cheese", "milk", "butter", "cream", "yogurt",
    "lemon", "lime", "apple", "banana", "avocado", "coconut", "peanut", "almond",
    "sugar", "honey", "chocolate", "vanilla", "cinnamon", "ginger", "basil",
    "oregano", "parsley", "cilantro", "chili", "curry", "soy", "olive", "oil",
    "vinegar", "bacon", "sausage", "tofu", "turkey", "duck", "squid", "crab",
    "lobster", "cabbage", "lettuce", "cucumber", "zucchini", "eggplant", "peas",
    "celery", "beet", "radish", "asparagus", "cauliflower", "kale", "mint",
    "thyme", "rosemary", "paprika", "cumin", "turmeric", "mustard", "mayonnaise",
    "ketchup", "salsa", "cream cheese", "mozzarella", "parmesan", "cheddar",
    "mushroom", "bell pepper", "green onion", "spring onion", "sweet potato",
]

SEPARATORS = [" and ", ", ", " with ", " plus ", " & "]
PREFIXES = ["", "I have ", "recipes with ", "make something with ", "what can I cook with "]
SUFFIXES = ["", " please", " for dinner", " recipe"]

VOCAB_SIZE = len(INGREDIENTS)


def build_features(text: str) -> np.ndarray:
    text = text.lower()
    features = np.zeros(VOCAB_SIZE, dtype=np.float32)
    for i, ingredient in enumerate(INGREDIENTS):
        if ingredient in text:
            features[i] = 1.0
    return features


def generate_sample() -> tuple[np.ndarray, np.ndarray]:
    count = random.randint(1, 4)
    picked = random.sample(INGREDIENTS, count)
    phrase_parts = random.choice(SEPARATORS).join(picked)
    text = random.choice(PREFIXES) + phrase_parts + random.choice(SUFFIXES)

    x = build_features(text)
    y = np.zeros(VOCAB_SIZE, dtype=np.float32)
    for ing in picked:
        y[INGREDIENTS.index(ing)] = 1.0

    # Light noise: sometimes omit one ingredient from features
    if random.random() < 0.15 and count > 1:
        idx = random.choice([INGREDIENTS.index(i) for i in picked])
        x[idx] = 0.0

    return x, y


def generate_dataset(n: int = 8000) -> tuple[np.ndarray, np.ndarray]:
    xs, ys = [], []
    for _ in range(n):
        x, y = generate_sample()
        xs.append(x)
        ys.append(y)
    return np.stack(xs), np.stack(ys)


def main() -> None:
    ASSETS.mkdir(parents=True, exist_ok=True)

    vocab_path = ASSETS / "ingredients_vocab.txt"
    vocab_path.write_text("\n".join(INGREDIENTS) + "\n", encoding="utf-8")
    print(f"Wrote {vocab_path} ({VOCAB_SIZE} ingredients)")

    x_train, y_train = generate_dataset(8000)
    x_val, y_val = generate_dataset(1000)

    model = tf.keras.Sequential([
        tf.keras.layers.Input(shape=(VOCAB_SIZE,)),
        tf.keras.layers.Dense(96, activation="relu"),
        tf.keras.layers.Dropout(0.1),
        tf.keras.layers.Dense(VOCAB_SIZE, activation="sigmoid"),
    ])
    model.compile(optimizer="adam", loss="binary_crossentropy", metrics=["accuracy"])
    model.fit(x_train, y_train, validation_data=(x_val, y_val), epochs=30, batch_size=64, verbose=1)

    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_model = converter.convert()

    model_path = ASSETS / "recipe_assistant.tflite"
    model_path.write_bytes(tflite_model)
    print(f"Wrote {model_path} ({len(tflite_model)} bytes)")


if __name__ == "__main__":
    main()
