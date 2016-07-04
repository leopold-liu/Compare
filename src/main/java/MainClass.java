import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by leopold on 2016/7/4.
 */
public class MainClass {

    public static void main(String[] args) {
        //程序开关
        boolean flag = true;
        //百度搜索总的结果数
        BaiduSearchNum baiduSearchNum = new BaiduSearchNum();
        //百度搜索消耗时间
        BaiduSearchTime baiduSearchTime = new BaiduSearchTime();
        //Google搜索总的结果数
        GoogleSearchNum googleSearchNum = new GoogleSearchNum();
        //Google搜索消耗时间
        GoogleSearchTime googleSearchTime = new GoogleSearchTime();
        //存放搜索结果的ArrayList
        ArrayList<String> baiduSearchResult = new ArrayList<String>();
        ArrayList<String> googleSearchResult = new ArrayList<String>();
        //Pattern p用来过滤搜索结果中的URL, 只保留URL中的主要部分, 用以计算重合度
        Pattern p = Pattern.compile("[[\\w][\\d][.]]+");
        Matcher m;
        Scanner scanner = new Scanner(System.in);

        //初始化Selenium Chrome driver所需的系统属性
        System.setProperty("webdriver.chrome.driver", "D:/chromedriver.exe");
        WebDriver driver;
        WebDriverWait wait;

        while (flag) {
            System.out.println("是否开始进行Baidu,Google搜索关键字对比? Y/N");
            String answer = scanner.nextLine();
            if (answer.equals("N") | answer.equals("n")){
                break;
            }
            
            System.out.println("请输入你想要比较的搜索关键字");
            String keyword = scanner.nextLine();

            //打开百度首页, 输入关键字完成搜索
            String urlBaidu = "https://www.baidu.com/";
            driver = new ChromeDriver();
            wait = new WebDriverWait(driver, 10);
            driver.get(urlBaidu);
            driver.findElement(By.cssSelector(BaiduHomePageResource.css_input)).sendKeys(keyword);
            driver.findElement(By.cssSelector(BaiduHomePageResource.css_submit)).click();

            //进入搜索页面, 等待页面展示完全, 记录搜索时间以及搜索结果个数
            long baiduSearchStartTime = System.currentTimeMillis();
            WebElement bSearchNum = wait.until(new ExpectedCondition<WebElement>() {
                public WebElement apply(WebDriver input) {
                    return input.findElement(By.cssSelector(BaiduSearchPageResource.css_num));
                }
            });
            long baiduSearchEndTime = System.currentTimeMillis();
            baiduSearchTime.setTime(baiduSearchStartTime, baiduSearchEndTime);
            baiduSearchNum.setNum(bSearchNum.getText());

            //收集搜索结果的url放进baiduSearchResult,写代码时, 百度搜索结果页面默认每页十个搜索结果
            WebElement baiduResult;
            for (int i = 1; i < 11; i++) {
                try {
                    baiduResult = driver.findElement(By.cssSelector(String.format("div[id='%s']", i)))
                            .findElement(By.className("c-showurl"));
                    baiduSearchResult.add(baiduResult.getText());
                } catch (org.openqa.selenium.NoSuchElementException e) {
                    //经测试,百度搜索结果中的URL极少数存在特殊情况,可能无法获取
                }
            }
            for (int i = 0 ; i < baiduSearchResult.size(); i++) {
                String s1 = baiduSearchResult.get(i);
                //从URL中剔除http:// 和 https://,避免影响重合度的计算
                if(s1.startsWith("http://")){
                    s1 = s1.substring(7);
                }
                if(s1.startsWith("https://")){
                    s1 = s1.substring(8);
                }
                //运用正则表达式过滤, 只保留URL的主要部分
                m = p.matcher(s1);
                if (m.find()) {
                    baiduSearchResult.set(i,m.group());
                }
            }

            //打开谷歌首页, 输入关键字完成搜索
            String urlGoogle = "https://www.google.de/?gws_rd=ssl";
            driver.get(urlGoogle);
            driver.findElement(By.cssSelector(GoogleHomePageResource.css_input)).sendKeys(keyword);
            driver.findElement(By.cssSelector(GoogleHomePageResource.css_submit)).click();

            //进入搜索页面, 等待页面展示完全, 记录搜索时间以及搜索结果个数
            long googleSearchStartTime = System.currentTimeMillis();
            WebElement gSearchNum = wait.until(new ExpectedCondition<WebElement>() {
                public WebElement apply(WebDriver input) {
                    return input.findElement(By.cssSelector(GoogleSearchPageResource.css_nums));
                }
            });
            long googleSearchEndTime = System.currentTimeMillis();
            googleSearchTime.setTime(googleSearchStartTime, googleSearchEndTime);
            googleSearchNum.setNum(gSearchNum.getText());

            //收集搜索结果的url放进googleSearchResult
            List<WebElement> googleResult = driver.findElements(By.cssSelector(GoogleSearchPageResource.css_result));
            for (WebElement w : googleResult) {
                String s2 = w.getText();
                //从URL中剔除http:// 和 https://,避免影响重合度的计算
                if(s2.startsWith("http://")){
                    s2 = s2.substring(7);
                }
                if(s2.startsWith("https://")){
                    s2 = s2.substring(8);
                }
                //运用正则表达式过滤, 只保留URL的主要部分
                m = p.matcher(s2);
                if (m.find()) {
                    googleSearchResult.add(m.group());
                }
            }

            //关闭当前浏览器页面
            if (null!=driver){
                driver.close();
            }

            //比较Baidu和Google搜索结果的重合度, URL主要部分如果有包含的情况, 记为重合度+1
            ArrayList<String> chongHeDu = new ArrayList<String>();
            for (String s1 : baiduSearchResult) {
                //排除异常情况取到的空URL
                if(!"".equals(s1)&null!=s1) {
                    for (String s2 : googleSearchResult) {
                        if (s2.contains(s1) | s1.contains(s2)) {
                            chongHeDu.add(s1);
                            break;
                        }
                    }
                }
            }

            //控制台打印搜索结果比较的详细情况
            System.out.println(baiduSearchNum.getNum());
            System.out.println(googleSearchNum.getNum());
            System.out.println("百度搜索耗时" + baiduSearchTime.getTime() + "ms");
            System.out.println("Google搜索耗时" + googleSearchTime.getTime() + "ms");
            System.out.println("百度和Google重合度为" + chongHeDu.size());
            if (chongHeDu.size()>0) {
                System.out.println("重合的网站为以下网站:");
                for (String s : chongHeDu) {
                    System.out.println(s);
                }
            }
        }
    }

}
