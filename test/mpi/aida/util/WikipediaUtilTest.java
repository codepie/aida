package mpi.aida.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class WikipediaUtilTest {

  @Test
  public void test() {
    String content = "{{Infobox scientist\n" +
        "| name                    = Raghu Ramakrishnan\n" +
        "| image                   = \n" +
        "| image_size             = 150px\n" +
        "| caption                 =\n" +
        "| birth_date              =\n" +
        "| birth_place             = \n" +
        "| death_date              =\n" +
        "| death_place             =\n" +
        "| residence               =\n" +
        "| citizenship             =\n" +
        "| nationality             =\n" +
        "| ethnicity               =\n" +
        "| field                   = [[Computer Science]]\n" +
        "| work_institution        = [[University of Wisconsin–Madison]], [[Yahoo! Research]]\n" +
        "| alma_mater              = [[University of Texas]]\n" +
        "| doctoral_advisor        = \n" +
        "| doctoral_students       =\n" +
        "| known_for               = \n" +
        "| author_abbreviation_bot =\n" +
        "| author_abbreviation_zoo =\n" +
        "| prizes                  =\n" +
        "| religion                =\n" +
        "| footnotes               =\n" +
        "}}\n" +
        "'''Raghu Ramakrishnan''' is a renowned researcher in the areas of database and information management.  He is currently a Vice President and Research Fellow for [[Yahoo! Inc.]]  Previously, he was a Professor of [http://www.cs.wisc.edu Computer Sciences] at the [[University of Wisconsin–Madison]].\n" +
        "\n" +
        "Ramakrishnan received a bachelor's degree from IIT Madras in 1983, and a Ph.D. from the University of Texas at Austin in 1987.  He has been selected as a Fellow of the ACM and a Packard fellow, and has done pioneering research in the areas of deductive databases, data mining, exploratory data analysis, data privacy, and web-scale data integration.  The focus of his current work (2007) is community-based information management.\n" +
        "\n" +
        "With [[Johannes Gehrke]], he authored the popular textbook [http://www.cs.wisc.edu/~dbbook Database Management Systems], also known as the \"Cow Book\".\n" +
        "\n" +
        "==External links==\n" +
        "*[http://www.cs.wisc.edu/~raghu Raghu's Wisconsin homepage]\n" +
        "*[http://research.yahoo.com/~ramakris Raghu's Yahoo! homepage]\n" +
        "\n" +
        "{{Persondata <!-- Metadata: see [[Wikipedia:Persondata]]. -->\n" +
        "| NAME              = Ramakrishnan, Raghu\n" +
        "| ALTERNATIVE NAMES =\n" +
        "| SHORT DESCRIPTION =\n" +
        "| DATE OF BIRTH     =\n" +
        "| PLACE OF BIRTH    =\n" +
        "| DATE OF DEATH     =\n" +
        "| PLACE OF DEATH    =\n" +
        "}}\n" +
        "{{DEFAULTSORT:Ramakrishnan, Raghu}}\n" +
        "[[Category:Fellows of the Association for Computing Machinery]]\n" +
        "[[Category:Database researchers]]\n" +
        "[[Category:Living people]]\n" +
        "[[Category:Data miners]]\n" +
        "[[Category:Yahoo! employees]]\n";
    
      String clean = WikipediaUtil.cleanWikipediaArticle(content);
      String expectecClean = " Raghu Ramakrishnan is a renowned researcher in the areas of database and information management. He is currently a Vice President and Research Fellow for Yahoo! Inc. Previously, he was a Professor of at the University of Wisconsin–Madison. Ramakrishnan received a bachelor's degree from IIT Madras in 1983, and a Ph.D. from the University of Texas at Austin in 1987. He has been selected as a Fellow of the ACM and a Packard fellow, and has done pioneering research in the areas of deductive databases, data mining, exploratory data analysis, data privacy, and web-scale data integration. The focus of his current work (2007) is community-based information management. With Johannes Gehrke, he authored the popular textbook , also known as the \"Cow Book\". * * ";
      assertEquals(expectecClean, clean);
  }
}
