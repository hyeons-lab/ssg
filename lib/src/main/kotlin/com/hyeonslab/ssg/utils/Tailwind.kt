/*
 * Copyright 2024-2026 Hyeons' Lab
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyeonslab.ssg.utils

/**
 * Type-safe Tailwind CSS utility classes organized by category.
 *
 * This sealed interface hierarchy provides compile-time safe access to commonly used Tailwind CSS
 * classes. Instead of using string literals that could contain typos, you can use these type-safe
 * constants.
 *
 * ## Available Categories
 *
 * ### Text Sizes
 * - `Tailwind.Text.Size.sm` - Small text (text-sm)
 * - `Tailwind.Text.Size.2xl` - 2XL text (text-2xl)
 * - `Tailwind.Text.Size.3xl` - 3XL text (text-3xl)
 * - `Tailwind.Text.Size.4xl` - 4XL text (text-4xl)
 *
 * ### Background Colors
 * - `Tailwind.Colors.Background.Violet.50` - Light violet background (bg-violet-50)
 * - `Tailwind.Colors.Background.Neutral.50` - Light neutral background (bg-neutral-50)
 * - `Tailwind.Colors.Background.Neutral.100` - Neutral 100 background (bg-neutral-100)
 *
 * ### Text Colors
 * - `Tailwind.Colors.Text.Neutral.600` - Neutral 600 text color (text-neutral-600)
 * - `Tailwind.Colors.Text.Neutral.900` - Neutral 900 text color (text-neutral-900)
 * - `Tailwind.Colors.Text.Custom("text-blue-500")` - Custom text color
 *
 * ## Usage Example
 *
 * ```kotlin
 * div(classes = "${Tailwind.Text.Size.sm.size} ${Tailwind.Colors.Text.Neutral.600.color}") {
 *     +"Small neutral text"
 * }
 * ```
 *
 * ## Note
 *
 * This is not an exhaustive list of all Tailwind classes. It contains only the most commonly used
 * classes in this library. For other Tailwind classes, use string literals with proper validation.
 */
sealed interface Tailwind {
  sealed interface Text : Tailwind {
    sealed interface Size : Text {
      val size: String

      data object sm : Size {
        override val size: String = "text-sm"
      }

      data object `2xl` : Size {
        override val size: String = "text-2xl"
      }

      data object `3xl` : Size {
        override val size: String = "text-3xl"
      }

      data object `4xl` : Size {
        override val size: String = "text-4xl"
      }
    }
  }

  sealed interface Colors {
    val color: String

    sealed interface Background : Colors {
      sealed interface Violet : Background {
        data object `50` : Violet {
          override val color: String = "bg-violet-50"
        }
      }

      sealed interface Neutral : Background {
        data object `50` : Neutral {
          override val color: String = "bg-neutral-50"
        }

        data object `100` : Neutral {
          override val color: String = "bg-neutral-100"
        }
      }
    }

    sealed interface Text : Colors {
      data class Custom(override val color: String) : Text

      sealed interface Neutral : Text {
        data object `600` : Neutral {
          override val color: String = "text-neutral-600"
        }

        data object `900` : Neutral {
          override val color: String = "text-neutral-900"
        }
      }
    }
  }
}
