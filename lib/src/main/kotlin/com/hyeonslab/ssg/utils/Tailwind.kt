package com.hyeonslab.ssg.utils

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
        }
    }

    sealed interface Colors {
        val color: String

        sealed interface Background : Colors {
            sealed interface Violet : Colors {
                data object `50` : Violet {
                    override val color: String = "bg-violet-50"
                }
            }

            sealed interface Neutral : Colors {
                data object `50` : Background {
                    override val color: String = "bg-neutral-50"
                }

                data object `100` : Background {
                    override val color: String = "bg-neutral-100"
                }
            }
        }

        sealed interface Text : Colors {
            sealed interface Neutral : Colors {
                data object `600` : Text {
                    override val color: String = "text-neutral-600"
                }

                data object `900` : Text {
                    override val color: String = "text-neutral-900"
                }
            }
        }
    }
}

data class TailwindColor(
    val color: String,
)
