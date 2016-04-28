package com.ferhtaydn.biocemid.annotators.word2vec

import com.ferhtaydn.biocemid.annotators.AnnotatorConfig

final case class Word2vecAnnotatorConfig(w2vDir: String, suffix: String, beforeAfterCount: Int,
  mainThreshold: Double, smallThreshold: Double, outputFileSuffix: String) extends AnnotatorConfig
