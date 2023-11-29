import scala.Tuple2;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import com.google.common.collect.Lists;
import java.util.*;
import java.util.regex.Pattern;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.math.RoundingMode;

public class WordGraph {
public static void main(String[] args) {
        if (args.length < 2) {
                System.err.println("Usage: WordGraph <input_file> <output_file>");
                System.exit(1);
        }
        SparkConf conf = new SparkConf().setAppName("WordGraph");
        JavaSparkContext sc = new JavaSparkContext(conf);

        JavaRDD<String> textFile = sc.textFile(args[0]);
        JavaRDD<String> lines = textFile.flatMap(s -> Arrays.asList(s.split("\n")).iterator());

        JavaPairRDD<String, String> pairs = lines.flatMapToPair(s -> {
                String[] words = s.replaceAll("[^a-zA-Z0-9\\s]", " ").toLowerCase().split("\\s+");
                List<Tuple2<String, String>> wordPairs = new ArrayList<>();
                for (int i = 0; i < words.length - 1; i++) {
                        wordPairs.add(new Tuple2<>(words[i], words[i + 1]));
                }
                return wordPairs.iterator();
        });

        JavaPairRDD<Tuple2<String, String>, Integer> coOccurrences = pairs.mapToPair(pair -> new Tuple2<>(pair, 1))
                .reduceByKey((a, b) -> a + b);

        JavaPairRDD<String, Iterable<Tuple2<String, Double>>> weightedEdges = coOccurrences.mapToPair(pair -> {
                String pWord = pair._1()._1();
                String sWord = pair._1()._2();
                int count = pair._2();
                return new Tuple2<>(pWord, new Tuple2<>(sWord, (double) count));
        }).groupByKey().mapValues(iter -> {
                List<Tuple2<String, Double>> weightedList = new ArrayList<>();
                int totalCount = 0;
                for (Tuple2<String, Double> tuple : iter) {
                        totalCount += tuple._2();
                }
                for (Tuple2<String, Double> tuple : iter) {
                        double weight = tuple._2() / totalCount;
                        weightedList.add(new Tuple2<>(tuple._1(), weight));
                }
                return weightedList;
        });

        // Output the result to a text file
        List<Tuple2<String, Iterable<Tuple2<String, Double>>>> outputData = weightedEdges.collect();
        DecimalFormat df = new DecimalFormat("#.000");
        df.setRoundingMode(RoundingMode.HALF_UP);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
                for (Tuple2<String, Iterable<Tuple2<String, Double>>> data : outputData) {
                        String predecessor = data._1();
                        Iterable<Tuple2<String, Double>> successors = data._2();
                        int count = 0;
                        for (Tuple2<String, Double> successor : successors) {
                                count++;
                        }
                        writer.write(predecessor + " " + count);
                        writer.newLine();
                        for (Tuple2<String, Double> successor : successors) {
                                String succ2 = df.format(successor._2());
                                writer.write("<" + successor._1() + ", " + succ2 + ">");
                                writer.newLine();
                        }
                }
                writer.close();
        } catch (IOException e) {
                e.printStackTrace();
        }
        sc.stop();
        }
}