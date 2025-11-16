package com.monopoly.cli

import java.io.File

/**
 * HTMLをPDFに変換
 *
 * 外部ツール（wkhtmltopdf）に依存
 */
class PdfExporter {
    /**
     * HTMLファイルをPDFに変換
     *
     * @param htmlFile 入力HTMLファイル
     * @param pdfFile 出力PDFファイル
     * @return 成功時はPDFファイル、失敗時はエラー
     */
    fun export(htmlFile: File, pdfFile: File): Result<File> {
        // wkhtmltopdfの存在確認
        val checkProcess = ProcessBuilder("which", "wkhtmltopdf")
            .redirectErrorStream(true)
            .start()

        val checkResult = checkProcess.waitFor()
        if (checkResult != 0) {
            return Result.failure(
                IllegalStateException(
                    "wkhtmltopdf not found. Please install it:\n" +
                    "  Ubuntu/Debian: sudo apt-get install wkhtmltopdf\n" +
                    "  macOS: brew install wkhtmltopdf\n" +
                    "  Windows: Download from https://wkhtmltopdf.org/"
                )
            )
        }

        // HTML → PDF変換
        val convertProcess = ProcessBuilder(
            "wkhtmltopdf",
            "--enable-local-file-access",
            htmlFile.absolutePath,
            pdfFile.absolutePath
        ).redirectErrorStream(true).start()

        val output = convertProcess.inputStream.bufferedReader().readText()
        val exitCode = convertProcess.waitFor()

        if (exitCode != 0) {
            return Result.failure(
                RuntimeException("PDF conversion failed:\n$output")
            )
        }

        return Result.success(pdfFile)
    }
}
