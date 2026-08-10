# TensorFlow Lite Recipe Assistant

## Model files (place in `app/src/main/assets/`)

| File | Purpose |
|------|---------|
| `recipe_assistant.tflite` | On-device multi-label ingredient classifier |
| `ingredients_vocab.txt` | One ingredient per line (model output labels) |

## Generate the model

```bash
pip install tensorflow
python scripts/generate_recipe_assistant_model.py
```

This writes both files into `app/src/main/assets/`.

## Model architecture

- **Type:** Custom multi-label classifier (not a cloud LLM)
- **Input:** `[1, N]` float32 — keyword presence vector built from user text + vocabulary (`N` = vocab size)
- **Output:** `[1, N]` float32 — sigmoid probability per ingredient
- **Hidden layer:** Dense(96, ReLU) → Dense(N, Sigmoid)

## Training dataset (synthetic)

The script generates ~8,000 synthetic phrases, for example:

- `"I have chicken and rice"`
- `"tomato, onion, garlic"`
- `"what can I cook with egg and cheese"`

Labels are the ingredients mentioned in each phrase. This teaches the model to refine keyword detection and handle noisy input.

## Runtime flow

1. User types in **Chat** tab
2. `TfliteIngredientAnalyzer` tokenizes text and builds keyword features
3. **TFLite Interpreter** runs on device (offline)
4. Detected ingredients search **TheMealDB** (or Room cache when offline)
5. Recipe cards appear in the chat bubble

If `recipe_assistant.tflite` is missing, keyword matching still works as a fallback until you run the generator script.
