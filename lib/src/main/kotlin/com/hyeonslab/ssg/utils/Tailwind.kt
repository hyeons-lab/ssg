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
