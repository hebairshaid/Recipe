package com.recipe.chat.data.ai

import android.content.Context
import com.recipe.chat.domain.repository.IngredientAnalyzer
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

/**
 * On-device ingredient detector powered by TensorFlow Lite.
 *
 * Model file: app/src/main/assets/recipe_assistant.tflite
 * Vocabulary: app/src/main/assets/ingredients_vocab.txt
 *
 * Generate the model with:
 *   pip install tensorflow
 *   python scripts/generate_recipe_assistant_model.py
 */
@Singleton
class TfliteIngredientAnalyzer @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : IngredientAnalyzer {

    private val vocabulary: List<String> = loadVocabulary()
    private val interpreter: Interpreter? = loadInterpreter()
    private val threshold = 0.45f

    override fun analyze(text: String): List<String> {
        if (vocabulary.isEmpty()) return emptyList()

        val features = buildKeywordFeatures(text)
        val scores = runModel(features)

        return vocabulary.indices
            .mapNotNull { index ->
                val score = scores[index]
                if (score >= threshold) vocabulary[index] else null
            }
            .distinct()
    }

    private fun buildKeywordFeatures(text: String): FloatArray {
        val normalized = text.lowercase()
        return FloatArray(vocabulary.size) { index ->
            if (vocabulary[index] in normalized) 1f else 0f
        }
    }

    private fun runModel(features: FloatArray): FloatArray {
        val model = interpreter
        if (model == null) {
            return features.copyOf()
        }

        val inputSize = model.getInputTensor(0).shape()[1]
        val outputSize = model.getOutputTensor(0).shape()[1]
        val input = Array(1) { FloatArray(inputSize) }
        val output = Array(1) { FloatArray(outputSize) }

        for (i in 0 until min(features.size, inputSize)) {
            input[0][i] = features[i]
        }

        model.run(input, output)

        return output[0].copyOf(min(outputSize, vocabulary.size))
    }

    private fun loadVocabulary(): List<String> {
        return runCatching {
            context.assets.open(VOCAB_FILE).bufferedReader().useLines { lines ->
                lines.map { it.trim().lowercase() }
                    .filter { it.isNotBlank() }
                    .toList()
            }
        }.getOrDefault(emptyList())
    }

    private fun loadInterpreter(): Interpreter? {
        return runCatching {
            val buffer = loadModelFile(MODEL_FILE)
            Interpreter(buffer)
        }.getOrNull()
    }

    private fun loadModelFile(fileName: String): MappedByteBuffer {
        context.assets.openFd(fileName).use { fd ->
            FileInputStream(fd.fileDescriptor).use { stream ->
                return stream.channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    fd.startOffset,
                    fd.declaredLength,
                )
            }
        }
    }

    private companion object {
        const val MODEL_FILE = "recipe_assistant.tflite"
        const val VOCAB_FILE = "ingredients_vocab.txt"
    }
}
