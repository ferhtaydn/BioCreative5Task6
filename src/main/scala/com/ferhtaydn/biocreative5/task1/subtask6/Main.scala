package com.ferhtaydn.biocreative5.task1.subtask6

object Main extends App {

  val annotatedDirectory = "manual_annotated_data_set"
  val annotationDirectory = "xml/bc5_dataset"
  val algoResultsDirectory = "annotated_before_after_results"

  Console.println(

    s"""
      |------------ Welcome to the BioCreative V - BioC Task - Subtask 6 ------------
      |
      |Please press the key for:
      |
      |1 - To generate the helper information files and tf-rf results from training data in $annotatedDirectory
      |
      |2 - To get tf-rf results from PSI-MI ontology definitions
      |
      |3 - Annotate the raw BioC files in $annotationDirectory
      |
      |4 - Generate Eval results in $algoResultsDirectory
      |
      |5 - Count of each method annotated in $algoResultsDirectory
      |
      |6 - To generate word2vec results for each method name and synonym
      |
    """.stripMargin

  )

  val selection = scala.io.StdIn.readInt()

  if (selection == 1) {

    BioC.methodNames.foreach(createHelperFiles)
    BioC.methodNames.foreach(calculateTfrf)

    def calculateTfrf(method: String): Unit = {

      println(s"method id: $method")

      val passagesFile = s"MI${method}_annotations_passages.txt"
      val tokenizedFile = s"MI${method}_tokenized_words.txt"
      val tfRfTokenizedFile = s"MI${method}_tokenized_tf-rf.txt"

      val tokenFreqs = BioC.calcFrequenciesFromTokensFile(tokenizedFile)

      val positivePassages = IO.read(passagesFile)
      val negativePassagesFiles = IO.listOthers(method, "annotations_passages.txt")

      val tfrf = BioC.tfRf(tokenFreqs, positivePassages, negativePassagesFiles).sortBy(_._2).reverse

      IO.write(tfRfTokenizedFile, Utils.stringifyTuple2Sequence(tfrf))
    }

    def createHelperFiles(method: String): Unit = {

      val passagesFile = s"MI${method}_annotations_passages.txt"
      val sentencesFile = s"MI${method}_annotations_sentences.txt"
      val tokenizedFile = s"MI${method}_tokenized_words.txt"
      val tokenizedFreqsFile = s"MI${method}_tokenized_freqs.txt"

      def out(annotatedSentences: String): Unit = {
        IO.append(passagesFile, annotatedSentences)
        IO.append(sentencesFile, Utils.mkSentence(annotatedSentences))
        IO.append(tokenizedFile, Utils.tokenize(Utils.mkSentence(annotatedSentences)).mkString("\n"))
      }

      IO.remove(passagesFile)
      IO.remove(sentencesFile)
      IO.remove(tokenizedFile)

      IO.list(annotatedDirectory, ".xml").foreach(f ⇒ out(BioC.extractAnnotatedSentences(f, method)))

      IO.write(tokenizedFreqsFile, Utils.stringifyTuple2Sequence(BioC.calcFrequenciesFromTokensFile(tokenizedFile)))

    }

  } else if (selection == 2) {

    BioC.methodsInfo.foreach { m ⇒

      val tfRfTokenizedFile = s"MI${m.id}_ontology_tf-rf.txt"

      val tokenFreqs = BioC.calcTokenFrequencies(Utils.tokenize(m.definition))

      val negativePassages = BioC.methodsInfo.filter(_.id != m.id).map(_.definition).map(Utils.tokenize(_).toSet).toSeq

      val tfrf = BioC.tfRfOntology(tokenFreqs, Seq(m.definition), negativePassages).sortBy(_._2).reverse

      IO.write(tfRfTokenizedFile, Utils.stringifyTuple2Sequence(tfrf))

    }

  } else if (selection == 3) {

    BioC.annotate(annotationDirectory, ".xml", "passages_with_exp_methods_with_before_after.xml")

  } else if (selection == 4) {

    BioC.evaluate(annotatedDirectory, algoResultsDirectory, ".xml")

  } else if (selection == 5) {

    BioC.countOfMethods(annotatedDirectory, ".xml")

  } else if (selection == 6) {

    val methods = BioC.methodsInfo.map { m ⇒
      val synonyms = m.synonym.map(x ⇒ x.split(" ").mkString("_"))
      val name = m.name.split(" ").mkString("_")
      if (!synonyms.contains(name)) name :: synonyms else name :: (synonyms diff List(name))
    }

    methods.foreach(println)

    import sys.process._
    methods.map { m ⇒
      val seq = Seq("/Users/aydinf/Desktop/word2vec_extension/distance_files",
        "/Users/aydinf/Desktop/bc3_word2vec_results/phrase1_eval/bc3_phrase1_vectors.bin") ++ m
      seq.!!
    }

  } else {

    Console.println("Please select the options from 1 until 4.")
    System.exit(0)

  }

}
