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

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EncodingTest :
  StringSpec({
    "escapeXml should escape all five XML entities in a single pass" {
      escapeXml("<a href=\"x\">'y' & \"z\"</a>") shouldBe
        "&lt;a href=&quot;x&quot;&gt;&apos;y&apos; &amp; &quot;z&quot;&lt;/a&gt;"
    }
    "escapeXml should return input without special chars unchanged" {
      escapeXml("https://example.com/about.html") shouldBe "https://example.com/about.html"
    }

    "encodeUrlPath should encode reserved path characters while preserving slashes" {
      encodeUrlPath("docs/my page.html") shouldBe "docs/my%20page.html"
      encodeUrlPath("a/b/c.html") shouldBe "a/b/c.html"
    }

    "normalizedPathString should unify backslashes and collapse dot segments" {
      normalizedPathString("docs\\nested\\..\\guide.html") shouldBe "docs/guide.html"
      normalizedPathString("./top.html") shouldBe "top.html"
      normalizedPathString("docs//double.html") shouldBe "docs/double.html"
    }

    "pageDepth and depthPrefix should compute accurate relative prefixes" {
      pageDepth("index.html") shouldBe 0
      depthPrefix("index.html") shouldBe "./"
      pageDepth("docs/guide.html") shouldBe 1
      depthPrefix("docs/guide.html") shouldBe "../"
      pageDepth("docs/nested/guide.html") shouldBe 2
      depthPrefix("docs/nested/guide.html") shouldBe "../../"
    }

    "relativeAssetHref should prefix relative targets and preserve absolute URIs and root-relative paths" {
      relativeAssetHref("docs/guide.html", "css/app.css") shouldBe "../css/app.css"
      relativeAssetHref("docs/guide.html", "./css/app.css") shouldBe "../css/app.css"
      relativeAssetHref("docs/guide.html", "css\\app.css") shouldBe "../css/app.css"
      relativeAssetHref("docs/guide.html", "/css/app.css") shouldBe "/css/app.css"
      relativeAssetHref("docs/guide.html", "\\css\\app.css") shouldBe "/css/app.css"
      relativeAssetHref("docs/guide.html", "https://cdn.example.com/app.css") shouldBe
        "https://cdn.example.com/app.css"
      relativeAssetHref("docs/guide.html", "data:image/png;base64,AAA") shouldBe
        "data:image/png;base64,AAA"
      relativeAssetHref("docs/guide.html", "mailto:user@example.com") shouldBe
        "mailto:user@example.com"
      relativeAssetHref("docs/guide.html", "css/app.css?v=2") shouldBe "../css/app.css?v=2"
    }
  })
