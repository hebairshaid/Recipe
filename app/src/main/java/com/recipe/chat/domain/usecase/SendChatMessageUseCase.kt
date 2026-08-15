package com.recipe.chat.domain.usecase

import com.recipe.chat.domain.model.ChatIntent
import com.recipe.chat.domain.model.ChatIntentParser
import com.recipe.chat.domain.repository.ChatAssistantRepository
import com.recipe.chat.domain.repository.IngredientAnalyzer
import com.recipe.domain.model.Recipe
import javax.inject.Inject

data class AssistantReply(
    val message: String,
    val ingredients: List<String>,
    val recipes: List<Recipe>,
)

class SendChatMessageUseCase @Inject constructor(
    private val ingredientAnalyzer: IngredientAnalyzer,
    private val chatAssistantRepository: ChatAssistantRepository,
) {
    suspend operator fun invoke(userMessage: String): AssistantReply {
        val clean = userMessage.trim()
        if (clean.isBlank()) {
            return reply(
                message = "Tell me what you want to cook!\n\n• List ingredients: chicken, rice, tomato\n• Or ask: suggest a pasta recipe",
            )
        }

        val ingredients = ingredientAnalyzer.analyze(clean)
        val request = ChatIntentParser.parse(clean, ingredients)

        return when (request.intent) {
            ChatIntent.GREETING -> reply(
                message = "Hello! I can suggest recipes for you.\n\nSend ingredients like chicken, rice, tomato — or ask things like:\n• Suggest a recipe\n• Give me pasta ideas",
            )

            ChatIntent.HELP -> reply(
                message = "Here's what I can do:\n\n" +
                    "1. Ingredients — type what you have\n" +
                    "   Example: egg, cheese, spinach\n\n" +
                    "2. Suggest — ask for ideas\n" +
                    "   Example: suggest a dinner recipe\n\n" +
                    "3. Search — name a dish or style\n" +
                    "   Example: give me pasta recipes",
            )

            ChatIntent.SUGGEST_GENERAL -> {
                val recipes = chatAssistantRepository.suggestRecipes()
                if (recipes.isEmpty()) {
                    reply("I couldn't find suggestions right now. Try again or list your ingredients.")
                } else {
                    reply(
                        message = "Here are some recipe ideas for you:",
                        recipes = recipes,
                    )
                }
            }

            ChatIntent.SUGGEST_KEYWORD -> {
                val keyword = request.keyword.orEmpty()
                val recipes = chatAssistantRepository.suggestRecipes(keyword)
                if (recipes.isEmpty()) {
                    reply("No recipes found for \"$keyword\". Try different words or list ingredients.")
                } else {
                    reply(
                        message = "Here are recipes I found for \"$keyword\":",
                        recipes = recipes,
                    )
                }
            }

            ChatIntent.BY_INGREDIENTS -> {
                val recipes = chatAssistantRepository.findRecipesByIngredients(ingredients)
                val ingredientText = ingredients.joinToString(", ")
                if (recipes.isEmpty()) {
                    reply(
                        message = "I detected $ingredientText but couldn't find matching recipes. Try different ingredients or check your connection.",
                        ingredients = ingredients,
                    )
                } else {
                    reply(
                        message = "Based on $ingredientText, here are recipes you can make:",
                        ingredients = ingredients,
                        recipes = recipes,
                    )
                }
            }
        }
    }

    private fun reply(
        message: String,
        ingredients: List<String> = emptyList(),
        recipes: List<Recipe> = emptyList(),
    ) = AssistantReply(message = message, ingredients = ingredients, recipes = recipes)
}
