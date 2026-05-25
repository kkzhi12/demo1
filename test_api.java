import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class test_api {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

        // API 1: datacenter securities api
        String url1 = "https://datacenter.eastmoney.com/securities/api/data/get?"
                + "type=RPT_F10_FINANCE_MAINFINADATA_BANK&sty=ALL"
                + "&filter=(SECUCODE%3D%22601398.SH%22)"
                + "&p=1&ps=5&sr=-1&st=REPORT_DATE&source=HSF10&client=PC";

        System.out.println("=== Testing API 1 ===");
        System.out.println("URL: " + url1);
        String result1 = doGet(client, url1);
        System.out.println("Response (first 500 chars): " + result1.substring(0, Math.min(500, result1.length())));
        System.out.println();

        // API 2: datacenter v1 api with reportName
        String url2 = "https://datacenter.eastmoney.com/securities/api/data/v1/get?"
                + "reportName=RPT_F10_FINANCE_MAINFINADATA&columns=ALL"
                + "&filter=(SECUCODE%3D%22601398.SH%22)"
                + "&pageNumber=1&pageSize=5&sortTypes=-1&sortColumns=REPORT_DATE";

        System.out.println("=== Testing API 2 ===");
        System.out.println("URL: " + url2);
        String result2 = doGet(client, url2);
        System.out.println("Response (first 500 chars): " + result2.substring(0, Math.min(500, result2.length())));
        System.out.println();

        // API 3: emweb F10 finance data
        String url3 = "https://emweb.securities.eastmoney.com/PC_HSF10/NewFinanceAnalysis/ZYZBAjaxAction?"
                + "fullCode=SH601398&ctype=4&maxCount=5";

        System.out.println("=== Testing API 3 (emweb) ===");
        System.out.println("URL: " + url3);
        String result3 = doGet(client, url3);
        System.out.println("Response (first 1000 chars): " + result3.substring(0, Math.min(1000, result3.length())));
    }

    static String doGet(HttpClient client, String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Referer", "https://emweb.eastmoney.com/")
                .header("Accept", "application/json, text/plain, */*")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + response.statusCode());
        return response.body();
    }
}
