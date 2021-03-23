package amata1219.narou.analyzer;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.stream.Collectors;

public class NarouAnalyzer {

    public static void main(String[] args) {
        List<Integer> limits = Arrays.asList(10, 100, 200, 300, 500);
        List<String> orderTypes = Arrays.asList("yearlypoint", "quarterpoint", "monthlypoint", "weeklypoint");
        for (int limit : limits) {
            for (String orderType : orderTypes) {
                outputAsTextFile(orderType + "\\" + limit + " - 2021-03-23.txt", analyzeKeywordsTrends(limit, orderType));
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("end");
    }

    private static void outputAsTextFile(String pathNameContinuation, List<String> lines) {
        File file = new File("C:\\Users\\Admin\\Desktop\\メモ\\narou analysis\\" + pathNameContinuation);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<String> analyzeKeywordsTrends(int limit, String orderType) {
        if (limit < 1 || 500 < limit) throw new IllegalArgumentException("limit must be [1, 500]");

        HashMap<String, Integer> keywordsAppearancesCounts = new HashMap<>();
        boolean isKeywordsSection = false;
        for (String line : narouInformationStream("lim=" + limit, "order=" + orderType)) {
            if (line.equals("  keyword: >")) {
                isKeywordsSection = true;
                continue;
            }

            if (!isKeywordsSection) continue;

            if (!line.startsWith("    ")) {
                isKeywordsSection = false;
                continue;
            }

            String[] keywords = line.substring(4).split(" ");
            for (String keyword : keywords) keywordsAppearancesCounts.merge(keyword, 1, Integer::sum);
        }

        return keywordsAppearancesCounts.entrySet().stream()
            .sorted(
                    Map.Entry.<String, Integer>comparingByValue().reversed()
                    .thenComparing(Map.Entry.comparingByKey())
            ).map(entry -> {
                String keyword = entry.getKey();
                int count = entry.getValue();
                return count + " (" + String.format("%.2f", ((double) count / (double) limit * 100.0)) + "%) - " + keyword;
            }).collect(Collectors.toList());
    }

    private static List<String> narouInformationStream(String... parameters) {
        List<String> lines = new ArrayList<>();
        try {
            URL url = new URL("https://api.syosetu.com/novelapi/api/?" + String.join("&", parameters));
            URLConnection connection = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String temp;
            while ((temp = reader.readLine()) != null) lines.add(temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

}
