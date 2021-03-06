package org.jitsi.rtp

import io.kotlintest.shouldBe
import io.kotlintest.specs.ShouldSpec
import java.nio.ByteBuffer

internal class RtpTwoByteHeaderExtensionTest : ShouldSpec() {
    override fun isInstancePerTest(): Boolean = true
    init {
        //  0                   1                   2                   3
        //  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
        // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
        // |       0x10    |    0x00       |           length=3            |
        // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
        // |      ID       |     L=0       |     ID        |     L=1       |
        // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
        // |       data    |    0 (pad)    |       ID      |      L=4      |
        // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
        // |                          data                                 |
        // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
        "parsing" {
            "and extension with length 0" {
                val length0Extension = ByteBuffer.wrap(
                    byteArrayOf(
                        0x01, 0x00
                    )
                )
                val ext = RtpTwoByteHeaderExtension(length0Extension)
                should("have the right id, size and data") {
                    ext.id shouldBe 1
                    ext.data.limit() shouldBe 0
                }
                should("parse to the end of the extension") {
                    length0Extension.remaining() shouldBe 0
                }
                "and then serializing it" {
                    val buf = ByteBuffer.allocate(24)
                    ext.serializeToBuffer(buf)
                    should("have written the correct amount of data") {
                        buf.position() shouldBe 2
                    }
                    should("have written the right id, size, and data") {
                        buf.rewind()
                        // id
                        buf.get().toInt() shouldBe 1
                        // length
                        buf.get().toInt() shouldBe 0
                    }
                }
            }
            "an extension with padding" {
                val extensionWithPadding = ByteBuffer.wrap(byteArrayOf(
                    0x01, 0x03, 0x42, 0x42,
                    0x42, 0x00, 0x00, 0x00
                ))
                val ext = RtpTwoByteHeaderExtension(extensionWithPadding)
                should("have the right id, size and data") {
                    ext.id shouldBe 1
                    ext.data.limit() shouldBe 3
                    repeat(3) {
                        ext.data.get() shouldBe 0x42.toByte()
                    }
                }
                should("parse to the end of the extensions") {
                    extensionWithPadding.remaining() shouldBe 0
                }
                "and then serializing it" {
                    val buf = ByteBuffer.allocate(24)
                    ext.serializeToBuffer(buf)
                    should("have written the correct amount of data") {
                        buf.position() shouldBe 5
                    }
                    should("have written the right id, size, and data") {
                        buf.rewind()
                        // id
                        buf.get().toInt() shouldBe 1
                        // length
                        buf.get().toInt() shouldBe 3
                        repeat(3) {
                            buf.get() shouldBe 0x42.toByte()
                        }
                    }
                }
            }
        }
    }
}
