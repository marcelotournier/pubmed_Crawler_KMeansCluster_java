// loading packages for creating arrays, lists, and text extraction and cleaning:

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import org.jsoup.Jsoup; // Jsoup jar library v1.11.3 downloaded from https://jsoup.org/
import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.text.Normalizer;
import java.lang.Math;

/**
 *  This is a Java program with the purpose of extracting and analyzing Pubmed articles about Diabetes.
 *  
 *  Here are the key tasks executed by the program:
 *  - Retrieve at least 10 different pages of text from Pubmed
 *  - Clean, tokenize and normalize the page to optimally get only the actual textual content
 *  - Represent the pages as vectors of binary values, using a simple hashing trick (token is present in the article or not).
 *  - Run a simple (1 iteration) K-means implementation built from scratch, to cluster those pages into 3 groups
 *  - Present results and discuss briefly some similarities between the pages in the same cluster
 *  - Identify key characteristics of pages in different clusters
 *  
 *  If you have any questions or suggestions, please send an email to dr.tournier@gmail.com
 *  
 *  Thank you!
 */
public class HtmlKMeansCluster {
    /**
     * In this method, we are using the JSoup library to extract the titles and the div class "abstr" from 
     * Pubmed.gov abstract articles.  These articles were selected typing "diabetes" as keyword, and filtering for free-fulltext
     * availability. This method will return an Array of Strings with the extracted abstracts.
     */
    public String[] htmlRetrieve(List urls) throws IOException {
        String[] articleList = new String[10];
        int cell = 0;
        int articleNum = 0;
        System.out.println("Done.");
        System.out.println("\n*** Retrieving article Titles ***");
        
        for(Object url : urls){
            
            Document doc = Jsoup.connect(url.toString()).get();
          
          // here we have a suggestion for future implementations including the article's title
          
          String title = doc.title(); // Title is needed
          if (title.endsWith(" - PubMed - NCBI")) {
              title = title.substring(0, title.length() - 16);
              System.out.println("Article "+(articleNum+1)+": "+title);
            }
           
          // selecting text body only in the "Abstract"
          Elements body = doc.getElementsByClass("abstr");
          // taking the the abstract text
          String bodyText = body.text();
          // as we have text starting with "Abstract", let's clean it upfront
            if (bodyText.startsWith("Abstract ")) {
              bodyText = bodyText.substring(9, bodyText.length()).toLowerCase();
            }
            articleList[cell] = bodyText;
            cell = cell + 1;
            articleNum++;
        }
    
        return articleList;
    }
    
    /**
     * In this method, we are using regexp and a list of stopwords to unnest and clean the tokens from the text.
     * As a result, we will have a List Array with one List of cleansed tokens for each article in each Array slot.
     * Details on token normalization in the comments within this method.
     */ 
    public List[] cleanTokenize(String[] texts) throws IOException {
        
        List[] cleanTokens = new List[10];
        // This is the original list from Python nltk stopwords, with "" and " " added for token cleaning purposes:
            String[] nltkWords = new String[] {"i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", 
                "yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its", 
                "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this", "that", "these", 
                "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does", 
                "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", 
                "for", "with", "about", "against", "between", "into", "through", "during", "before", "after", "above", "below",
                "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", 
                "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", 
                "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don",
                "should", "now"," ",""};
            List<String> stopWords = Arrays.asList(nltkWords);
            // Created a regexp to remove all non-alphabetic characters, keeping spaces intact
            
            for(int i=0; i<10;i++){
            
            String cleanText = texts[i].toString().replaceAll("[^A-Za-z ]", " ").trim();
            
            // remove spaces and stopwords:
 
            String[] textArray = cleanText.split(" ");
            List<String> wordList = Arrays.asList(textArray);
            
            List<String> cleanList = new ArrayList();
            
            for(String s : wordList){
                if(!stopWords.contains(s)){ 
              // token canonization - Normalization Form C (NFC): Canonical Decomposition, followed by Canonical Composition
              // ref: https://docs.oracle.com/javase/tutorial/i18n/text/normalizerapi.html
              s = Normalizer.normalize(s, Normalizer.Form.NFC);
              cleanList.add(s);
            }
          }
          cleanTokens[i] = cleanList;
        }

        return cleanTokens;
    }
    
    /**
     * In this method, we are using a simple hashing method to build the feature vectors.
     * 
     * The approach chosen was to building a vector containing all unique tokens from all articles, then
     * using this vector of unique tokens to build a token vector for each one of the 10 articles.
     * 
     * As hashing strategy, we built vectors of binaries (0: token NOT in the abstract ; 1: token in the abstract), 
     * for the sake of improving model explainability.
     * 
     * Finally, this method returns an Array with 10 vectors of binaries, representing the tokens present in the article.
     */ 
    public List[] hashTokens(List[] texts) throws IOException {
        List[] hashedTokens = new List[10];
        
        List uniqueTokens = new ArrayList();
        for(List articleTokens : texts){
            for(Object token : articleTokens){
                String tokenString = token.toString();
                if(!uniqueTokens.contains(token)){
                    uniqueTokens.add(token);
                }
                
            }
        }
        
        //System.out.println(uniqueTokens); // for debugging purposes only
        //int count = 0;
        for(int i=0; i<10;i++){
            // creates a vector for tokens
            List vectorizedTokens = new ArrayList();
            // now, let's check if each token appears on each article based on the uniqueTokens:
            int wordCount = 0;
            for (Object token : uniqueTokens){
                String tokenString = token.toString();
                if(!texts[i].contains(tokenString)){
                    vectorizedTokens.add(1.0); 
                } 
                else {
                    vectorizedTokens.add(0.0);
                    }
                /*
                wordCount = Collections.frequency(texts[i], tokenString);
                double counter = (double) wordCount;
                vectorizedTokens.add(counter);*/
            }
            
            hashedTokens[i] = vectorizedTokens;
        }
        
        //hashedTokens[1] = uniqueTokens;
        
        return hashedTokens;
    }
    
    /**
     * In this method, we build a K-Means Cluster implementation from scratch.
     * 
     * Further details about model hyperparameters can be found in the comments below.
     * 
     * This method will execute the KMeans algorithm and will print the distance for each of the 3 centroids, and the 
     * cluster membership assigned for each article as a result, as well.
     */ 
    public void kMeansCluster(List[] arrays) throws IOException {
        List clusterList = new ArrayList();
        
        
        
        /* we will build the algorithm with the following assumptions:
         * K=3;
         * Single Linkage
         * Euclidean distance
         * Number of iterations=1
         * Centroid reference points: 1st 5th and 10th article
        */
         // selecting centroids
         List<Double> centroid1 = arrays[0];
         List<Double> centroid2 = arrays[4];
         List<Double> centroid3 = arrays[9];
         
         // variables to storing results in each distance computation:
         Double result1 = 0.0;
         Double result2 = 0.0;
         Double result3 = 0.0;
         
         List<Double> memberList = new ArrayList();
         // running the computations for the 10 arrays:
         for(int i=0; i<10;i++){
         // generating 1 iteration for each article:
         System.out.print("\nDistances for Article "+(i+1)+" - ");
         List<Double> arrayList = arrays[i];
            // reseting variables for results in each iteration:  
                result1 = 0.0;
                result2 = 0.0;
                result3 = 0.0;
                // calculating distances in each cluster:  
             for (int j=0; j<arrays[0].size();j++){
                 //computating distances for each column
                 double point1 = (double) arrayList.get(j);
                 //System.out.println("p1: "+point1); // for debugging purposes
                 double point2 = (double) centroid1.get(j);
                 //System.out.println("p2: "+point2); // for debugging purposes
                 //System.out.println("sumproduct: "+((point1 - point2)*(point1 - point2))); // for debugging purposes
                 
                 result1 = result1 +((point1 - point2)*(point1 - point2));
                 //System.out.println("new sum: "+result1); // for debugging purposes
                }
                //System.out.println(result1); // for debugging
                
                // printing distance for result1:
                result1 = Math.sqrt(result1);
                result1 = Math.round(result1 * 100.0) / 100.0;
                System.out.print("Centroid 0: "+result1+" - ");
            
             for (int j=0; j<arrays[0].size();j++){
                 //computating distances for each column
                 double point1 = (double) arrayList.get(j);
                 double point2 = (double) centroid2.get(j);
                 result2 = result2 + ((point1 - point2)*(point1 - point2));
                }
                //System.out.println(result2); // for debugging
                
                // printing distance for result2:
                result2 = Math.sqrt(result2);
                result2 = Math.round(result2 * 100.0) / 100.0;
                System.out.print("Centroid 1: "+result2+" - ");

             for (int j=0; j<arrays[0].size();j++){
                 //computating distances for each column
                 double point1 = (double) arrayList.get(j);
                 double point2 = (double) centroid3.get(j);
                 result3 = result3 + ((point1 - point2)*(point1 - point2));
                }
                //System.out.println(result3); // for debugging
                
                // printing distance for result3:
                result3 = Math.sqrt(result3);
                result3 = Math.round(result3 * 100.0) / 100.0;
                System.out.print("Centroid 2: "+result3+" - ");
                
                // printing cluster membership:
                String member;
                    if (result1 <= result2 && result1 <= result3) {
                        member = "Cluster: 0";
                    } else if (result2 <= result3 && result2 <= result1) {
                        member = "Cluster: 1";
                    } else {
                        member = "Cluster: 2";
                    }
                System.out.print(member);
        }
         
    }
    
    /** 
     * These URLs are from the abstracts that were extracted from Pubmed website (https://pubmed.gov), 
     * using the keyword "diabetes", and selecting free-full-text-articles
       */
    public List<String> testURLs() throws IOException {
  
        List<String> urlList = new ArrayList();

        urlList.add("https://www.ncbi.nlm.nih.gov/pubmed/31064043");
        urlList.add("https://www.ncbi.nlm.nih.gov/pubmed/31064037");
        urlList.add("https://www.ncbi.nlm.nih.gov/pubmed/31063991");
        urlList.add("https://www.ncbi.nlm.nih.gov/pubmed/31063971");
        urlList.add("https://www.ncbi.nlm.nih.gov/pubmed/31063480"); // this is a really rellevant scientific study
        urlList.add("https://www.ncbi.nlm.nih.gov/pubmed/31063470");
        urlList.add("https://www.ncbi.nlm.nih.gov/pubmed/31063459");
        urlList.add("https://www.ncbi.nlm.nih.gov/pubmed/31063261");
        urlList.add("https://www.ncbi.nlm.nih.gov/pubmed/31063201");
        urlList.add("https://www.ncbi.nlm.nih.gov/pubmed/31061403");
        
        return urlList;
    }
    
        /** 
     * Finally, in the main method, we build the data pipeline, connecting each method in linear sequence.
     * 
     * Discussion and comments are also printed here.
       */
    public static void main (String[] args) throws IOException {
        HtmlKMeansCluster h1 = new HtmlKMeansCluster();
        
        System.out.println("\n#####################################################################################################");
        System.out.println("Welcome to the BoilerPlate Version of PUBMED Article Crawler and Analyzer.");
        System.out.println("\nThis program is meant to extract and analyze 10 abstracts about Diabetes from http://pubmed.gov.");
        System.out.println("We used NLP techniques and a simple implementation of K-Means Clustering to segregate the abstracts.");
        System.out.println("(Please, see the source code for further details.)");
        System.out.println("Here are the results!\n");
        System.out.println("*** Retrieving web pages ***");
        
        // instantiating url list and text list:
        List pageList = h1.testURLs();
        
        String[] textList = h1.htmlRetrieve(pageList);
        
        List[] tokens = h1.cleanTokenize(textList);
        
        List[] hashTokenMatrix = h1.hashTokens(tokens);
        
        // for debugging related to tokens:
        //System.out.println(tokens);
        /*
        for(int i=0; i<10;i++){
            System.out.println(tokens[i]);
        }*/
        
        // for debugging or extracting the token vectors:
        /*
        for(int i=0; i<10;i++){
            System.out.println(hashTokenMatrix[i]);
        }*/
        
        System.out.println("\n*** K-Means Clustering Results ***");
        h1.kMeansCluster(hashTokenMatrix);
        
        System.out.println("\n\n*** Article Discussion ***\n");
        System.out.println("Cluster 0: \nThis 'group' consists on a single article, related to Aneurysmal Subarachnoid Hemorrhage.");
        System.out.println("This means that Pubmed search engine probably pulled the article because diabetes is a token within it,");
        System.out.println("but it is not on the main topic;\n");
        System.out.println("Cluster 1: \nArticles that seem to be related to studies about clinical outcomes & risk factors;\n");
        System.out.println("Cluster 2: \nThese studies seem to have in common a relationship with biochemical markers;");
        System.out.println("\n\n*** Limitations & Recommendations ***\n");
        System.out.println("This model has as limitations the small number of documents, and a simple strategy of data preparation.");
        System.out.println("The simplicity of a single-iteration K-Means Clustering algorithm also needs to be taken into account.");
        System.out.println("Finally, as improvement recommendations, it would be wise to run the algorithm on all articles from");
        System.out.println("Pubmed related to Diabetes, and try to test other K-values, analyzing the variance between groups to");
        System.out.println("find the best number of clusters for the problem. Finally, other unsupervised methods can be tried,");
        System.out.println("to check if further business and/or research value can be found for the company.\n");
        System.out.println("Finally, I would like to thank Apixio for the challenge. It was very nice to build an algorithm from");
        System.out.println("scratch, and noticing that even with very simple models, we can add business value in healthcare.");
        System.out.println("\n\n   ");
    }
}
