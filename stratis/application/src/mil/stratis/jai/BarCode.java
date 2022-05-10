//
// created from java4less rbarcode library
//

package mil.stratis.jai;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

@Slf4j
@SuppressWarnings("all") //This class was converted from rbarcode jar.  Too many issues to fix right now.
public class BarCode extends Canvas {

  public static final int BAR39 = 0;
  public static final int BAR39EXT = 1;
  public static final int MAT25 = 8;
  public static final int EAN13 = 10;
  public static final int EAN8 = 11;
  public static final int UPCE = 12;
  public static final int CODE93EXT = 14;
  public static final int PDF417 = 30;
  public static final int MSI_CHECKSUM_11_10 = 2;
  public static final int MSI_CHECKSUM_10_10 = 3;
  public static final boolean TEXT_ON_TOP = false;
  protected int barType = 0;
  protected String code = "";
  private String usedCodeSup = "";
  protected boolean checkCharacter = true;
  protected double postnetHeightTallBar = 0.25D;
  protected double postnetHeightShortBar = 0.125D;
  protected double leftMarginCM = 0.5D;
  protected double topMarginCM = 0.5D;
  protected String supplement = "";
  protected static final int D = 1;
  protected boolean guardBars = true;
  protected Color backColor;
  protected String codeText;
  protected int resolution;
  protected double barHeightCM;
  protected Font textFont;
  protected Color fontColor;
  protected Color barColor;
  protected char upcesytem;
  protected char codABarStartChar;
  protected char codABarStopChar;
  protected boolean upceanSupplement2;
  protected boolean upceanSupplement5;
  protected char code128Set;
  protected int msiChecksum;
  protected double doubleX;
  protected double doubleN;
  protected double doubleI;
  protected double doubleH;
  protected double doubleL;
  protected int rotate;
  protected double supSeparationCM;
  protected double supHeight;
  protected boolean processTilde;
  protected int currentX;
  protected int currentY;
  protected String[][] set39;
  protected String[][] set25;
  protected String[][] setMSI;
  protected String[][] set11;
  protected String[][] setCODABAR;
  protected String[][] set93;
  protected String[][] setUPCALeft;
  protected String[][] setUPCARight;
  protected String[][] setUPCEOdd;
  protected String[][] setUPCEEven;
  protected String[] set39Ext;
  protected String[] set93Ext;
  protected String[] upcesystem0;
  protected String[] upcesystem1;
  protected String[][] setEANLeftA;
  protected String[][] setEANLeftB;
  protected String[][] setEANRight;
  protected String[] setEANCode;
  protected String[] fiveSuplement;
  protected String[] set128;
  protected String[] set128A;
  protected String[] set128B;
  protected String[] set128C;
  protected String[][] setPOSTNET;
  protected int leftMarginPixels;
  protected int topMarginPixels;
  protected int narrowBarPixels;
  protected int widthBarPixels;
  protected double narrowBarCM;
  protected double widthBarCM;
  protected int barHeightPixels;
  private int extraHeight;
  private int leftGuardBar;
  private int centerGuardBarStart;
  private int centerGuardBarEnd;
  private int rightGuardBar;
  private int endOfCode;
  private int startSuplement;
  private int endSuplement;
  private int suplementTopMargin;

  public BarCode() {
    this.backColor = Color.white;
    this.codeText = "";
    this.resolution = 38;
    this.barHeightCM = 0.0D;
    this.textFont = new Font("Arial", Font.PLAIN, 11);
    this.fontColor = Color.black;
    this.barColor = Color.black;
    this.upcesytem = '1';
    this.codABarStartChar = 'A';
    this.codABarStopChar = 'B';
    this.upceanSupplement2 = false;
    this.upceanSupplement5 = false;
    this.code128Set = 'B';
    this.msiChecksum = 0;
    this.doubleX = 0.03D;
    this.doubleN = 2.0D;
    this.doubleI = 1.0D;
    this.doubleH = 0.45D;
    this.doubleL = 0.0D;
    this.rotate = 0;
    this.supSeparationCM = 0.5D;
    this.supHeight = 0.8D;
    this.processTilde = false;
    this.currentX = 0;
    this.currentY = 0;
    this.set39 = new String[][]{{"0", "nnnwwnwnn"}, {"1", "wnnwnnnnw"}, {"2", "nnwwnnnnw"}, {"3", "wnwwnnnnn"}, {"4", "nnnwwnnnw"}, {"5", "wnnwwnnnn"}, {"6", "nnwwwnnnn"}, {"7", "nnnwnnwnw"}, {"8", "wnnwnnwnn"}, {"9", "nnwwnnwnn"}, {"A", "wnnnnwnnw"}, {"B", "nnwnnwnnw"}, {"C", "wnwnnwnnn"}, {"D", "nnnnwwnnw"}, {"E", "wnnnwwnnn"}, {"F", "nnwnwwnnn"}, {"G", "nnnnnwwnw"}, {"H", "wnnnnwwnn"}, {"I", "nnwnnwwnn"}, {"J", "nnnnwwwnn"}, {"K", "wnnnnnnww"}, {"L", "nnwnnnnww"}, {"M", "wnwnnnnwn"}, {"N", "nnnnwnnww"}, {"O", "wnnnwnnwn"}, {"P", "nnwnwnnwn"}, {"Q", "nnnnnnwww"}, {"R", "wnnnnnwwn"}, {"S", "nnwnnnwwn"}, {"T", "nnnnwnwwn"}, {"U", "wwnnnnnnw"}, {"V", "nwwnnnnnw"}, {"W", "wwwnnnnnn"}, {"X", "nwnnwnnnw"}, {"Y", "wwnnwnnnn"}, {"Z", "nwwnwnnnn"}, {"-", "nwnnnnwnw"}, {".", "wwnnnnwnn"}, {" ", "nwwnnnwnn"}, {"$", "nwnwnwnnn"}, {"/", "nwnwnnnwn"}, {"+", "nwnnnwnwn"}, {"%", "nnnwnwnwn"}, {"*", "nwnnwnwnn"}};
    this.set25 = new String[][]{{"0", "nnwwn"}, {"1", "wnnnw"}, {"2", "nwnnw"}, {"3", "wwnnn"}, {"4", "nnwnw"}, {"5", "wnwnn"}, {"6", "nwwnn"}, {"7", "nnnww"}, {"8", "wnnwn"}, {"9", "nwnwn"}};
    this.setMSI = new String[][]{{"0", "nwnwnwnw"}, {"1", "nwnwnwwn"}, {"2", "nwnwwnnw"}, {"3", "nwnwwnwn"}, {"4", "nwwnnwnw"}, {"5", "nwwnnwwn"}, {"6", "nwwnwnnw"}, {"7", "nwwnwnwn"}, {"8", "wnnwnwnw"}, {"9", "wnnwnwwn"}};
    this.set11 = new String[][]{{"0", "nnnnw"}, {"1", "wnnnw"}, {"2", "nwnnw"}, {"3", "wwnnn"}, {"4", "nnwnw"}, {"5", "wnwnn"}, {"6", "nwwnn"}, {"7", "nnnww"}, {"8", "wnnwn"}, {"9", "wnnnn"}, {"-", "nnwnn"}};
    this.setCODABAR = new String[][]{{"0", "nnnnnww"}, {"1", "nnnnwwn"}, {"2", "nnnwnnw"}, {"3", "wwnnnnn"}, {"4", "nnwnnwn"}, {"5", "wnnnnwn"}, {"6", "nwnnnnw"}, {"7", "nwnnwnn"}, {"8", "nwwnnnn"}, {"9", "wnnwnnn"}, {"-", "nnnwwnn"}, {"$", "nnwwnnn"}, {":", "wnnnwnw"}, {"/", "wnwnnnw"}, {".", "wnwnwnn"}, {"+", "nnwnwnw"}, {"A", "nnwwnwn"}, {"B", "nwnwnnw"}, {"C", "nnnwnww"}, {"D", "nnnwwwn"}};
    this.set93 = new String[][]{{"0", "131112"}, {"1", "111213"}, {"2", "111312"}, {"3", "111411"}, {"4", "121113"}, {"5", "121212"}, {"6", "121311"}, {"7", "111114"}, {"8", "131211"}, {"9", "141111"}, {"A", "211113"}, {"B", "211212"}, {"C", "211311"}, {"D", "221112"}, {"E", "221211"}, {"F", "231111"}, {"G", "112113"}, {"H", "112212"}, {"I", "112311"}, {"J", "122112"}, {"K", "132111"}, {"L", "111123"}, {"M", "111222"}, {"N", "111321"}, {"O", "121122"}, {"P", "131121"}, {"Q", "212112"}, {"R", "212211"}, {"S", "211122"}, {"T", "211221"}, {"U", "221121"}, {"V", "222111"}, {"W", "112122"}, {"X", "112221"}, {"Y", "112121"}, {"Z", "123111"}, {"-", "121131"}, {".", "311112"}, {" ", "311211"}, {"$", "321111"}, {"/", "112131"}, {"+", "113121"}, {"%", "211131"}, {"_1", "121211"}, {"_2", "312111"}, {"_3", "311121"}, {"_4", "122211"}};
    this.setUPCALeft = new String[][]{{"0", "3211"}, {"1", "2221"}, {"2", "2122"}, {"3", "1411"}, {"4", "1132"}, {"5", "1231"}, {"6", "1114"}, {"7", "1312"}, {"8", "1213"}, {"9", "3112"}};
    this.setUPCARight = new String[][]{{"0", "3211"}, {"1", "2221"}, {"2", "2122"}, {"3", "1411"}, {"4", "1132"}, {"5", "1231"}, {"6", "1114"}, {"7", "1312"}, {"8", "1213"}, {"9", "3112"}};
    this.setUPCEOdd = new String[][]{{"0", "3211"}, {"1", "2221"}, {"2", "2122"}, {"3", "1411"}, {"4", "1132"}, {"5", "1231"}, {"6", "1114"}, {"7", "1312"}, {"8", "1213"}, {"9", "3112"}};
    this.setUPCEEven = new String[][]{{"0", "1123"}, {"1", "1222"}, {"2", "2212"}, {"3", "1141"}, {"4", "2311"}, {"5", "1321"}, {"6", "4111"}, {"7", "2131"}, {"8", "3121"}, {"9", "2113"}};
    this.set39Ext = new String[]{"%U", "$A", "$B", "$C", "$D", "$E", "$F", "$G", "$H", "$I", "$J", "$K", "$L", "$M", "$N", "$O", "$P", "$Q", "$R", "$S", "$T", "$U", "$V", "$W", "$X", "$Y", "$Z", "%A", "%B", "%C", "%D", "%E", " ", "/A", "/B", "/C", "/D", "/E", "/F", "/G", "/H", "/I", "/J", "/K", "/L", "-", ".", "/O", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "/Z", "%F", "%G", "%H", "%I", "%J", "%V", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "%K", "%L", "%M", "%N", "%O", "%W", "+A", "+B", "+C", "+D", "+E", "+F", "+G", "+H", "+I", "+J", "+K", "+L", "+M", "+N", "+O", "+P", "+Q", "+R", "+S", "+T", "+U", "+V", "+W", "+X", "+Y", "+Z", "%P", "%Q", "%R", "%S", "%T"};
    this.set93Ext = new String[]{"_2U", "_1A", "_1B", "_1C", "_1D", "_1E", "_1F", "_1G", "_1H", "_1I", "_1J", "_1K", "_1L", "_1M", "_1N", "_1O", "_1P", "_1Q", "_1R", "_1S", "_1T", "_1U", "_1V", "_1W", "_1X", "_1Y", "_1Z", "_2A", "_2B", "_2C", "_2D", "_2E", " ", "_3A", "_3B", "_3C", "_3D", "_3E", "_3F", "_3G", "_3H", "_3I", "_3J", "_3K", "_3L", "-", ".", "_3O", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "_3Z", "_2F", "_2G", "_2H", "_2I", "_2J", "_2V", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "_2K", "_2L", "_2M", "_2N", "_2O", "_2W", "_4A", "_4B", "_4C", "_4D", "_4E", "_4F", "_4G", "_4H", "_4I", "_4J", "_4K", "_4L", "_4M", "_4N", "_4O", "_4P", "_4Q", "_4R", "_4S", "_4T", "_4U", "_4V", "_4W", "_4X", "_4Y", "_4Z", "_2P", "_2Q", "_2R", "_2S", "_2T"};
    this.upcesystem0 = new String[]{"EEEOOO", "EEOEOO", "EEOOEO", "EEOOOE", "EOEEOO", "EOOEEO", "EOOOEE", "EOEOEO", "EOEOOE", "EOOEOE"};
    this.upcesystem1 = new String[]{"OOOEEE", "OOEOEE", "OOEEOE", "OOEEEO", "OEOOEE", "OEEOOE", "OEEEOO", "OEOEOE", "OEOEEO", "OEEOEO"};
    this.setEANLeftA = new String[][]{{"0", "3211"}, {"1", "2221"}, {"2", "2122"}, {"3", "1411"}, {"4", "1132"}, {"5", "1231"}, {"6", "1114"}, {"7", "1312"}, {"8", "1213"}, {"9", "3112"}};
    this.setEANLeftB = new String[][]{{"0", "1123"}, {"1", "1222"}, {"2", "2212"}, {"3", "1141"}, {"4", "2311"}, {"5", "1321"}, {"6", "4111"}, {"7", "2131"}, {"8", "3121"}, {"9", "2113"}};
    this.setEANRight = new String[][]{{"0", "3211"}, {"1", "2221"}, {"2", "2122"}, {"3", "1411"}, {"4", "1132"}, {"5", "1231"}, {"6", "1114"}, {"7", "1312"}, {"8", "1213"}, {"9", "3112"}};
    this.setEANCode = new String[]{"AAAAA", "ABABB", "ABBAB", "ABBBA", "BAABB", "BBAAB", "BBBAA", "BABAB", "BABBA", "BBABA"};
    this.fiveSuplement = new String[]{"EEOOO", "EOEOO", "EOOEO", "EOOOE", "OEEOO", "OOEEO", "OOOEE", "OEOEO", "OEOOE", "OOEOE"};
    this.set128 = new String[]{"212222", "222122", "222221", "121223", "121322", "131222", "122213", "122312", "132212", "221213", "221312", "231212", "112232", "122132", "122231", "113222", "123122", "123221", "223211", "221132", "221231", "213212", "223112", "312131", "311222", "321122", "321221", "312212", "322112", "322211", "212123", "212321", "232121", "111323", "131123", "131321", "112313", "132113", "132311", "211313", "231113", "231311", "112133", "112331", "132131", "113123", "113321", "133121", "313121", "211331", "231131", "213113", "213311", "213131", "311123", "311321", "331121", "312113", "312311", "332111", "314111", "221411", "431111", "111224", "111422", "121124", "121421", "141122", "141221", "112214", "112412", "122114", "122411", "142112", "142211", "241211", "221114", "413111", "241112", "134111", "111242", "121142", "121241", "114212", "124112", "124211", "411212", "421112", "421211", "212141", "214121", "412121", "111143", "111341", "131141", "114113", "114311", "411113", "411311", "113141", "114131", "311141", "411131"};
    this.set128A = new String[]{" ", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-", ".", "/", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "?", "@", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "[", "\\", "]", "^", "_", "\u0000", "\u0001", "\u0002", "\u0003", "\u0004", "\u0005", "\u0006", "\u0007", "\b", "\t", "\n", "\u000b", "\f", "\r", "\u000e", "\u000f", "\u0010", "\u0011", "\u0012", "\u0013", "\u0014", "\u0015", "\u0016", "\u0017", "\u0018", "\u0019", "\u001a", "\u001b", "\u001c", "\u001d", "\u001e", "\u001f", "_96", "_97", "_98", "_99", "_100", "_101", "_102"};
    this.set128B = new String[]{" ", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-", ".", "/", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "?", "@", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "[", "\\", "]", "^", "_", "`", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "{", "_92", "}", "~", "_95", "_96", "_97", "_98", "_99", "_100", "_101", "_102"};
    this.set128C = new String[]{"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "_100", "_101", "_102"};
    this.setPOSTNET = new String[][]{{"0", "11000"}, {"1", "00011"}, {"2", "00101"}, {"3", "00110"}, {"4", "01001"}, {"5", "01010"}, {"6", "01100"}, {"7", "10001"}, {"8", "10010"}, {"9", "10100"}};
    this.leftMarginPixels = 0;
    this.topMarginPixels = 0;
    this.narrowBarPixels = 0;
    this.widthBarPixels = 0;
    this.narrowBarCM = 0.0D;
    this.widthBarCM = 0.0D;
    this.barHeightPixels = 0;
    this.extraHeight = 0;
    this.leftGuardBar = 0;
    this.centerGuardBarStart = 0;
    this.centerGuardBarEnd = 0;
    this.rightGuardBar = 0;
    this.endOfCode = 0;
  }

  public Dimension getPaintedArea() {
    if (this.rotate == 90 || this.rotate == 270) {
      return new Dimension(this.currentY, this.currentX);
    }
    return new Dimension(this.currentX, this.currentY);
  }

  protected void addBar(Graphics g, int w, boolean black, int bartopmargin) {
    if (black) {
      g.setColor(this.barColor);
      g.fillRect(this.currentX, this.topMarginPixels + bartopmargin, w, this.barHeightPixels + this.extraHeight - bartopmargin);
    }

    this.currentX += w;
  }

  protected void paintPostNetChar(Graphics g, String pattern) {
    int shortBarPixels = (int) (this.postnetHeightShortBar * (double) this.resolution);
    int tallBarPixels = (int) (this.postnetHeightTallBar * (double) this.resolution);
    int diff = tallBarPixels - shortBarPixels;
    g.setColor(this.barColor);

    for (int i = 0; i < pattern.length(); ++i) {
      char cBar = pattern.charAt(i);
      if (cBar == '0') {
        g.fillRect(this.currentX, this.topMarginPixels + diff, this.narrowBarPixels, shortBarPixels + this.extraHeight);
      }

      if (cBar == '1') {
        g.fillRect(this.currentX, this.topMarginPixels, this.narrowBarPixels, tallBarPixels + this.extraHeight);
      }

      this.currentX += this.narrowBarPixels;
      this.currentX += this.widthBarPixels;
    }
  }

  protected void paintPOSTNET(Graphics g) {
    int pos;
    int sum = 0;
    String codetmp = this.code;
    this.paintPostNetChar(g, "1");

    String c;
    for (int i = this.code.length() - 1; i >= 0; --i) {
      c = "" + this.code.charAt(i);
      sum += this.findChar(this.setPOSTNET, c);
    }

    int checksum = (int) mod(sum, 10.0D);
    if (checksum != 0) {
      checksum = 10 - checksum;
    }

    if (this.checkCharacter) {
      codetmp = codetmp + checksum;
    }

    for (int i = 0; i < codetmp.length(); ++i) {
      c = "" + codetmp.charAt(i);
      pos = this.findChar(this.setPOSTNET, c);
      this.paintPostNetChar(g, this.setPOSTNET[pos][1]);
    }

    this.paintPostNetChar(g, "1");
  }

  protected int findChar(String[][] table, String c) {
    for (int i = 0; i < table.length; ++i) {
      if (c.compareTo(table[i][0]) == 0) {
        return i;
      }
    }

    return -1;
  }

  protected void paintInterleaved25(Graphics g) {
    int pos;
    int pos2;
    String codetmp = this.code;
    this.paintChar(g, "bwbw", "nnnn");
    if (mod(this.code.length(), 2.0D) == 0.0D && this.checkCharacter) {
      codetmp = "0" + this.code;
    }

    if (mod(this.code.length(), 2.0D) == 1.0D && !this.checkCharacter) {
      codetmp = "0" + this.code;
    }

    int sumeven = 0;
    int sumodd = 0;
    boolean even = true;

    String c;
    for (int i = codetmp.length() - 1; i >= 0; --i) {
      c = "" + codetmp.charAt(i);
      if (even) {
        sumeven += this.findChar(this.set25, c);
      }
      else {
        sumodd += this.findChar(this.set25, c);
      }

      even = !even;
    }

    int checksum = sumeven * 3 + sumodd;
    checksum = (int) mod(checksum, 10.0D);
    if (checksum != 0) {
      checksum = 10 - checksum;
    }

    if (this.checkCharacter) {
      codetmp = codetmp + checksum;
    }

    for (int i = 0; i < codetmp.length(); i += 2) {
      c = "" + codetmp.charAt(i);
      String c2 = "" + codetmp.charAt(i + 1);
      pos = this.findChar(this.set25, c);
      pos2 = this.findChar(this.set25, c2);

      for (int j = 0; j < 5; ++j) {
        this.paintChar(g, "b", "" + this.set25[pos][1].charAt(j));
        this.paintChar(g, "w", "" + this.set25[pos2][1].charAt(j));
      }
    }

    this.paintChar(g, "bwb", "wnn");
    if (this.codeText.length() == 0) {
      this.codeText = codetmp;
    }
  }

  protected void paintIND25(Graphics g) {
    int pos;
    String codetmp = this.code;
    this.paintChar(g, "bwbwbw", "wwwwnw");
    int sumeven = 0;
    int sumodd = 0;
    boolean even = true;

    String c;
    for (int i = codetmp.length() - 1; i >= 0; --i) {
      c = "" + codetmp.charAt(i);
      if (even) {
        sumeven += this.findChar(this.set25, c);
      }
      else {
        sumodd += this.findChar(this.set25, c);
      }

      even = !even;
    }

    int checksum = sumeven * 3 + sumodd;
    checksum = (int) mod(checksum, 10.0D);
    if (checksum != 0) {
      checksum = 10 - checksum;
    }

    if (this.checkCharacter) {
      codetmp = codetmp + checksum;
    }

    for (int i = 0; i < codetmp.length(); ++i) {
      c = "" + codetmp.charAt(i);
      pos = this.findChar(this.set25, c);
      if (pos >= 0) {
        for (int j = 0; j < this.set25[pos][1].length(); ++j) {
          this.paintChar(g, "b", "" + this.set25[pos][1].charAt(j));
          this.paintChar(g, "w", "w");
        }
      }
    }

    this.paintChar(g, "bwbwb", "wwnww");
  }

  protected String upceanCheck(String s) {
    boolean odd = true;
    int sumodd = 0;
    int sum = 0;

    for (int i = s.length() - 1; i >= 0; --i) {
      if (odd) {

        sumodd += Integer.parseInt(String.valueOf(s.charAt(i)));
      }
      else {
        sum += Integer.parseInt(String.valueOf(s.charAt(i)));
      }

      odd = !odd;
    }

    sum += sumodd * 3;
    int c = (int) mod(sum, 10.0D);
    if (c != 0) {
      c = 10 - c;
    }

    return "" + c;
  }

  protected void paintUPCA(Graphics g) {
    int pos;
    if (this.code.length() == 11 && this.checkCharacter) {
      this.code = this.code + this.upceanCheck(this.code);
    }

    if (this.codeText.length() == 0) {
      this.codeText = this.code;
    }

    this.paintGuardChar(g, "bwb", "nnn", 0);
    this.leftGuardBar = this.currentX;

    for (int i = 0; i < this.code.length(); ++i) {
      String c = "" + this.code.charAt(i);

      if (i <= 5) {
        pos = this.findChar(this.setUPCALeft, c);
        this.paintChar(g, "wbwb", this.setUPCALeft[pos][1]);
      }
      else {
        pos = this.findChar(this.setUPCARight, c);
        this.paintChar(g, "bwbw", this.setUPCARight[pos][1]);
      }

      if (i == 5) {
        this.centerGuardBarStart = this.currentX;
        this.paintGuardChar(g, "wbwbw", "nnnnn", 0);
        this.centerGuardBarEnd = this.currentX;
      }
    }

    this.rightGuardBar = this.currentX;
    this.paintGuardChar(g, "bwb", "nnn", 0);
    this.endOfCode = this.currentX;
    if (this.upceanSupplement2) {
      this.paintSup2(g, this.code.substring(1, 3));
    }
    else if (this.upceanSupplement5) {
      this.paintSup5(g, this.code.substring(1, 6));
    }
  }

  protected void paintEAN13(Graphics g) {
    int pos;
    if (this.code.length() == 12 && this.checkCharacter) {
      this.code = this.code + this.upceanCheck(this.code);
      if (this.codeText.length() == 0) {
        this.codeText = this.code;
      }
    }

    if (this.code.length() >= 13) {
      this.paintGuardChar(g, "bwb", "nnn", 0);
      this.leftGuardBar = this.currentX;
      String sets = this.setEANCode[Integer.parseInt(String.valueOf(this.code.charAt(0)))];
      pos = this.findChar(this.setEANLeftA, "" + this.code.charAt(1));
      this.paintChar(g, "wbwb", this.setEANLeftA[pos][1]);

      for (int i = 2; i < 12; ++i) {
        String c = "" + this.code.charAt(i);

        if (i <= 6) {
          String[][] leftset = this.setEANLeftA;
          if (sets.charAt(i - 2) == 'B') {
            leftset = this.setEANLeftB;
          }

          pos = this.findChar(leftset, c);
          this.paintChar(g, "wbwb", leftset[pos][1]);
        }
        else {
          pos = this.findChar(this.setEANRight, c);
          this.paintChar(g, "bwbw", this.setEANRight[pos][1]);
        }

        if (i == 6) {
          this.centerGuardBarStart = this.currentX;
          this.paintGuardChar(g, "wbwbw", "nnnnn", 0);
          this.centerGuardBarEnd = this.currentX;
        }
      }

      pos = this.findChar(this.setEANRight, "" + this.code.charAt(12));
      this.paintChar(g, "bwbw", this.setEANRight[pos][1]);
      this.rightGuardBar = this.currentX;
      this.paintGuardChar(g, "bwb", "nnn", 0);
      this.endOfCode = this.currentX;
      if (this.upceanSupplement2) {
        this.paintSup2(g, this.code.substring(2, 4));
      }
      else if (this.upceanSupplement5) {
        this.paintSup5(g, this.code.substring(2, 7));
      }
    }
  }

  private int findInArray(String[] s1, String c) {
    for (int j = 0; j < s1.length; ++j) {
      if (s1[j].compareTo(c) == 0) {
        return j;
      }
    }

    return -1;
  }

  private String convertCode128ControlChar(String c, boolean ean128mode) {
    String fnc1 = "Ê";
    String fnc2 = "Å";
    String fnc3 = "Ä";
    String fnc4A = "É";
    String fnc4B = "È";
    if (ean128mode && c.compareTo(" ") == 0) {
      c = "_102";
    }

    if (c.compareTo(fnc1) == 0) {
      c = "_102";
    }

    if (c.compareTo(fnc4A) == 0) {
      c = "_101";
    }

    if (c.compareTo(fnc4B) == 0) {
      c = "_100";
    }

    if (c.compareTo(fnc2) == 0) {
      c = "_97";
    }

    if (c.compareTo(fnc3) == 0) {
      c = "_96";
    }

    return c;
  }

  private boolean isDigit(String c) {
    if (c.length() > 1) {
      return false;
    }
    else {
      return c.charAt(0) >= '0' && c.charAt(0) <= '9';
    }
  }

  private int getNextLowerCase(String s, int i) {
    for (int j = i; j < s.length(); ++j) {
      if (s.charAt(j) >= 'a' && s.charAt(j) <= 'z') {
        return j;
      }
    }

    return 9999;
  }

  private int getNextControlChar(String s, int i) {
    for (int j = i; j < s.length(); ++j) {
      if (s.charAt(j) < ' ') {
        return j;
      }
    }

    return 9999;
  }

  private char calculateNextSet(String s, int i) {
    if (s.length() >= i + 4 && this.isDigit("" + s.charAt(i)) && this.isDigit("" + s.charAt(i + 1)) && this.isDigit("" + s.charAt(i + 2)) && this.isDigit("" + s.charAt(i + 3))) {
      return 'C';
    }
    else {
      return this.getNextControlChar(s, i) < this.getNextLowerCase(s, i) ? 'A' : 'B';
    }
  }

  protected void paintCode128(Graphics g) {
    int pos;
    int check;
    StringBuilder text = new StringBuilder();
    String[] set;

    int sum = 103;

    char currentSet = this.code128Set;
    if (this.code128Set == 'B') {
      sum = 104;
      this.paintChar(g, "bwbwbw", "211214");
    }

    if (this.code128Set == 'C') {
      sum = 105;
      this.paintChar(g, "bwbwbw", "211232");
    }

    if (this.code128Set != 'B' && this.code128Set != 'C') {
      this.paintChar(g, "bwbwbw", "211412");
    }

    int w = 1;

    for (int i = 0; i < this.code.length(); ++i) {
      set = this.set128A;
      if (currentSet == 'B') {
        set = this.set128B;
      }

      if (currentSet == 'C') {
        set = this.set128C;
      }

      String c = "" + this.code.charAt(i);
      c = this.convertCode128ControlChar(c, false);
      if ((c.charAt(0) == 201 || c.charAt(0) == 200) && currentSet != 'C') {
        if (i >= this.code.length() - 1) {
          break;
        }

        ++i;
        c = "" + this.code.charAt(i);
      }

      if (currentSet != 'C' && c.charAt(0) == 199) {
        pos = this.findInArray(this.set128B, "_99");
        if (currentSet == 'A') {
          pos = this.findInArray(this.set128A, "_99");
        }

        sum += pos * w;
        ++w;
        this.paintChar(g, "bwbwbw", this.set128[pos]);
        currentSet = 'C';
        set = this.set128C;
        ++i;
        c = "" + this.code.charAt(i);
      }

      boolean done;
      if (currentSet == 'C') {
        if (c.charAt(0) != 201 && c.charAt(0) != 200) {
          done = false;
          if (i < this.code.length() - 1 && this.isDigit("" + c) && this.isDigit("" + this.code.charAt(i + 1))) {
            c = c + this.code.charAt(i + 1);
            pos = this.findInArray(this.set128C, c);
            if (pos >= 0) {
              this.paintChar(g, "bwbwbw", this.set128[pos]);
              sum += pos * w;
            }

            done = true;
            ++i;
            text.append(c);
          }

          if (!done && !this.isDigit(c)) {
            pos = this.findInArray(this.set128C, c);
            if (pos >= 0) {
              this.paintChar(g, "bwbwbw", this.set128[pos]);
              sum += pos * w;
              done = true;
            }
          }

          if (!done && this.isDigit(c)) {
            currentSet = 'B';
            if (i < this.code.length() - 1 && this.getNextControlChar(this.code, i + 1) < this.getNextLowerCase(this.code, i + 1)) {
              currentSet = 'A';
            }

            pos = this.findInArray(this.set128C, "_100");
            if (currentSet == 'A') {
              pos = this.findInArray(this.set128C, "_101");
            }

            sum += pos * w;
            ++w;
            this.paintChar(g, "bwbwbw", this.set128[pos]);
            pos = this.findInArray(this.set128B, c);
            if (pos >= 0) {
              this.paintChar(g, "bwbwbw", this.set128[pos]);
              sum += pos * w;
            }

            done = true;
            text.append(c);
          }

          if (!done && !this.isDigit(c)) {
            currentSet = this.calculateNextSet(this.code, i);
            int pos2 = 0;
            if (currentSet == 'A') {
              pos2 = this.findInArray(set, "_101");
            }

            if (currentSet == 'B') {
              pos2 = this.findInArray(set, "_100");
            }

            this.paintChar(g, "bwbwbw", this.set128[pos2]);
            sum += pos2 * w;
            --i;
          }
        }
        else {
          if (c.charAt(0) == 201) {
            pos = this.findInArray(this.set128C, "_101");
            currentSet = 'A';
          }
          else {
            pos = this.findInArray(this.set128C, "_100");
            currentSet = 'B';
          }

          sum += pos * w;
          this.paintChar(g, "bwbwbw", this.set128[pos]);
        }
      }
      else {
        int pos2;
        if (this.calculateNextSet(this.code, i) == 'C') {
          --i;
          pos2 = this.findInArray(this.set128B, "_99");
          if (currentSet == 'A') {
            pos2 = this.findInArray(this.set128A, "_99");
          }

          this.paintChar(g, "bwbwbw", this.set128[pos2]);
          sum += pos2 * w;
          currentSet = 'C';
        }
        else {
          pos = this.findInArray(set, c);
          if (currentSet == 'A' && pos == -1 && this.findInArray(this.set128B, c) >= 0) {
            if (this.getNextControlChar(this.code, i) < this.getNextLowerCase(this.code, i)) {
              pos2 = this.findInArray(set, "_98");
            }
            else {
              pos2 = this.findInArray(set, "_100");
              currentSet = 'B';
            }

            this.paintChar(g, "bwbwbw", this.set128[pos2]);
            sum += pos2 * w;
            ++w;
            set = this.set128B;
          }

          if (currentSet == 'B' && pos == -1 && this.findInArray(this.set128A, c) >= 0) {
            if (this.getNextControlChar(this.code, i) > this.getNextLowerCase(this.code, i)) {
              pos2 = this.findInArray(set, "_98");
            }
            else {
              pos2 = this.findInArray(set, "_101");
              currentSet = 'A';
            }

            this.paintChar(g, "bwbwbw", this.set128[pos2]);
            sum += pos2 * w;
            ++w;
            set = this.set128A;
          }

          pos = this.findInArray(set, c);
          if (pos >= 0) {
            this.paintChar(g, "bwbwbw", this.set128[pos]);
            sum += pos * w;
            text.append(c);
          }
        }
      }

      ++w;
    }

    if (this.checkCharacter) {
      check = (int) mod(sum, 103.0D);
      this.paintChar(g, "bwbwbw", this.set128[check]);
    }

    this.paintChar(g, "bwbwbwb", "2331112");
    if (this.codeText.length() == 0) {
      this.codeText = text.toString();
    }
  }

  protected void paintEAN128(Graphics g) {
    int pos;
    int check;
    String userText = this.getEAN128Text(this.code);
    if (this.barType == 16) {
      this.code128Set = 'C';
    }

    String[] set;

    int sum = 103;

    char currentSet = this.code128Set;
    if (this.code128Set == 'B') {
      sum = 104;
      this.paintChar(g, "bwbwbw", "211214");
    }

    if (this.code128Set == 'C') {
      sum = 105;
      this.paintChar(g, "bwbwbw", "211232");
    }

    if (this.code128Set != 'B' && this.code128Set != 'C') {
      this.paintChar(g, "bwbwbw", "211412");
    }

    int w = 1;

    for (int i = 0; i < this.code.length(); ++i) {
      set = this.set128A;
      if (currentSet == 'B') {
        set = this.set128B;
      }

      if (currentSet == 'C') {
        set = this.set128C;
      }

      String c = "" + this.code.charAt(i);
      c = this.convertCode128ControlChar(c, true);

      if (this.barType == 16 && i == 0) {
        pos = this.findInArray(this.set128C, "_102");
        this.paintChar(g, "bwbwbw", this.set128[pos]);
        sum += pos * w;
        ++w;
      }

      boolean done;
      if (currentSet == 'C') {
        done = false;
        if (i < this.code.length() - 1 && this.isDigit("" + c) && this.isDigit("" + this.code.charAt(i + 1))) {
          c = c + this.code.charAt(i + 1);
          pos = this.findInArray(this.set128C, c);
          if (pos >= 0) {
            this.paintChar(g, "bwbwbw", this.set128[pos]);
            sum += pos * w;
          }

          done = true;
          ++i;
        }

        if (!done && !this.isDigit(c)) {
          pos = this.findInArray(this.set128C, c);
          if (pos >= 0) {
            this.paintChar(g, "bwbwbw", this.set128[pos]);
            sum += pos * w;
            done = true;
          }
        }

        if (!done && this.isDigit(c)) {
          currentSet = 'B';
          if (i < this.code.length() - 1 && this.getNextControlChar(this.code, i + 1) < this.getNextLowerCase(this.code, i + 1)) {
            currentSet = 'A';
          }

          pos = this.findInArray(this.set128C, "_100");
          if (currentSet == 'A') {
            pos = this.findInArray(this.set128C, "_101");
          }

          sum += pos * w;
          ++w;
          this.paintChar(g, "bwbwbw", this.set128[pos]);
          pos = this.findInArray(this.set128B, c);
          if (pos >= 0) {
            this.paintChar(g, "bwbwbw", this.set128[pos]);
            sum += pos * w;
          }

          done = true;
        }

        if (!done && !this.isDigit(c)) {
          currentSet = this.calculateNextSet(this.code, i);
          int pos2 = 0;
          if (currentSet == 'A') {
            pos2 = this.findInArray(set, "_101");
          }

          if (currentSet == 'B') {
            pos2 = this.findInArray(set, "_100");
          }

          this.paintChar(g, "bwbwbw", this.set128[pos2]);
          sum += pos2 * w;
          --i;
        }
      }
      else {
        int pos2;
        if (this.calculateNextSet(this.code, i) == 'C') {
          --i;
          pos2 = this.findInArray(this.set128B, "_99");
          if (currentSet == 'A') {
            pos2 = this.findInArray(this.set128A, "_99");
          }

          this.paintChar(g, "bwbwbw", this.set128[pos2]);
          sum += pos2 * w;
          currentSet = 'C';
        }
        else {
          pos = this.findInArray(set, c);
          if (currentSet == 'A' && pos == -1 && this.findInArray(this.set128B, c) >= 0) {

            if (this.getNextControlChar(this.code, i) < this.getNextLowerCase(this.code, i)) {
              pos2 = this.findInArray(set, "_98");
            }
            else {
              pos2 = this.findInArray(set, "_100");
              currentSet = 'B';
            }

            this.paintChar(g, "bwbwbw", this.set128[pos2]);
            sum += pos2 * w;
            ++w;
            set = this.set128B;
          }

          if (currentSet == 'B' && pos == -1 && this.findInArray(this.set128A, c) >= 0) {

            if (this.getNextControlChar(this.code, i) > this.getNextLowerCase(this.code, i)) {
              pos2 = this.findInArray(set, "_98");
            }
            else {
              pos2 = this.findInArray(set, "_101");
              currentSet = 'A';
            }

            this.paintChar(g, "bwbwbw", this.set128[pos2]);
            sum += pos2 * w;
            ++w;
            set = this.set128A;
          }

          pos = this.findInArray(set, c);
          if (pos >= 0) {
            this.paintChar(g, "bwbwbw", this.set128[pos]);
            sum += pos * w;
          }
        }
      }

      ++w;
    }

    if (this.checkCharacter) {
      check = (int) mod(sum, 103.0D);
      this.paintChar(g, "bwbwbw", this.set128[check]);
    }

    this.paintChar(g, "bwbwbwb", "2331112");
    if (this.codeText.length() == 0) {
      this.codeText = userText;
    }
  }

  protected void paintEAN8(Graphics g) {
    int pos;
    if (this.code.length() == 7 && this.checkCharacter) {
      this.code = this.code + this.upceanCheck(this.code);
    }

    if (this.code.length() >= 8) {
      if (this.codeText.length() == 0) {
        this.codeText = this.code;
      }

      this.paintGuardChar(g, "bwb", "nnn", 0);
      this.leftGuardBar = this.currentX;

      for (int i = 0; i < 8; ++i) {
        String c = "" + this.code.charAt(i);

        if (i <= 3) {
          pos = this.findChar(this.setEANLeftA, c);
          this.paintChar(g, "wbwb", this.setEANLeftA[pos][1]);
        }
        else {
          pos = this.findChar(this.setEANRight, c);
          this.paintChar(g, "bwbw", this.setEANRight[pos][1]);
        }

        if (i == 3) {
          this.centerGuardBarStart = this.currentX;
          this.paintGuardChar(g, "wbwbw", "nnnnn", 0);
          this.centerGuardBarEnd = this.currentX;
        }
      }

      this.rightGuardBar = this.currentX;
      this.paintGuardChar(g, "bwb", "nnn", 0);
      this.endOfCode = this.currentX;
      if (this.upceanSupplement2) {
        this.paintSup2(g, this.code.substring(2, 4));
      }
      else if (this.upceanSupplement5) {
        this.paintSup5(g, this.code.substring(2, 7));
      }
    }
  }

  protected void paintUPCE(Graphics g) {
    int pos;
    int checkchar;
    String codetmp = "";
    if (this.code.length() == 11 && this.checkCharacter) {
      this.code = this.code + this.upceanCheck(this.code);
    }

    if (this.code.length() >= 12) {
      checkchar = Integer.parseInt(String.valueOf(this.code.charAt(11)));
      if (this.code.substring(3, 6).compareTo("000") == 0 || this.code.substring(3, 6).compareTo("100") == 0 || this.code.substring(3, 6).compareTo("200") == 0) {
        codetmp = this.code.substring(1, 3) + this.code.substring(8, 11) + this.code.charAt(3);
      }

      if (this.code.substring(3, 6).compareTo("300") == 0 || this.code.substring(3, 6).compareTo("400") == 0 || this.code.substring(3, 6).compareTo("500") == 0 || this.code.substring(3, 6).compareTo("600") == 0 || this.code.substring(3, 6).compareTo("700") == 0 || this.code.substring(3, 6).compareTo("800") == 0 || this.code.substring(3, 6).compareTo("900") == 0) {
        codetmp = this.code.substring(1, 4) + this.code.substring(9, 11) + "3";
      }

      if (this.code.substring(4, 6).compareTo("10") == 0 || this.code.substring(4, 6).compareTo("20") == 0 || this.code.substring(4, 6).compareTo("30") == 0 || this.code.substring(4, 6).compareTo("40") == 0 || this.code.substring(4, 6).compareTo("50") == 0 || this.code.substring(4, 6).compareTo("60") == 0 || this.code.substring(4, 6).compareTo("70") == 0 || this.code.substring(4, 6).compareTo("80") == 0 || this.code.substring(4, 6).compareTo("90") == 0) {
        codetmp = this.code.substring(1, 5) + this.code.charAt(10) + "4";
      }

      if (this.code.substring(5, 6).compareTo("0") != 0) {
        codetmp = this.code.substring(1, 6) + this.code.charAt(10);
      }

      if (this.codeText.length() == 0) {
        this.codeText = codetmp;
      }

      this.paintGuardChar(g, "bwb", "nnn", 0);
      this.leftGuardBar = this.currentX;
      String system = this.upcesystem0[checkchar];
      if (this.upcesytem == '1') {
        system = this.upcesystem1[checkchar];
      }

      for (int i = 0; i < codetmp.length(); ++i) {
        String c = "" + codetmp.charAt(i);

        String[][] setLeft = this.setUPCEOdd;
        if (system.charAt(i) == 'E') {
          setLeft = this.setUPCEEven;
        }

        pos = this.findChar(setLeft, c);
        StringBuilder inverted = new StringBuilder();

        for (int j = 0; j < setLeft[pos][1].length(); ++j) {
          inverted.insert(0, setLeft[pos][1].charAt(j));
        }

        this.paintChar(g, "wbwb", inverted.toString());
      }

      this.rightGuardBar = this.currentX;
      this.paintGuardChar(g, "wbwbwb", "nnnnnn", 0);
      this.endOfCode = this.currentX;
      if (this.upceanSupplement2) {
        this.paintSup2(g, codetmp.substring(0, 2));
      }
      else if (this.upceanSupplement5) {
        this.paintSup5(g, codetmp.substring(0, 5));
      }
    }
  }

  protected void paintSup2(Graphics g, String chars) {
    if (this.supplement.length() > 0) {
      chars = this.supplement;
    }

    this.suplementTopMargin = (int) ((double) this.barHeightPixels * (1.0D - this.supHeight));
    if (this.usedCodeSup.length() == 0) {
      this.usedCodeSup = chars;
    }

    if (chars.length() == 2) {
      this.currentX = (int) ((double) this.currentX + (double) this.resolution * this.supSeparationCM);
      this.startSuplement = this.currentX;

      int i;
      try {
        i = Integer.parseInt(chars);
      }
      catch (Exception var7) {
        i = 0;
      }

      String parity = "OO";
      if (mod(i, 4.0D) == 1.0D) {
        parity = "OE";
      }

      if (mod(i, 4.0D) == 2.0D) {
        parity = "EO";
      }

      if (mod(i, 4.0D) == 3.0D) {
        parity = "EE";
      }

      this.paintGuardChar(g, "bwb", "112", this.suplementTopMargin);
      String[][] set = this.setUPCEOdd;
      if (parity.charAt(0) == 'E') {
        set = this.setUPCEEven;
      }

      int pos = this.findChar(set, "" + chars.charAt(0));
      this.paintGuardChar(g, "wbwb", set[pos][1], this.suplementTopMargin);
      this.paintGuardChar(g, "wb", "11", this.suplementTopMargin);
      set = this.setUPCEOdd;
      if (parity.charAt(1) == 'E') {
        set = this.setUPCEEven;
      }

      pos = this.findChar(set, "" + chars.charAt(1));
      this.paintGuardChar(g, "wbwb", set[pos][1], this.suplementTopMargin);
      this.endSuplement = this.currentX;
    }
  }

  protected void paintSup5(Graphics g, String chars) {
    if (this.supplement.length() > 0) {
      chars = this.supplement;
    }

    this.suplementTopMargin = (int) ((double) this.barHeightPixels * (1.0D - this.supHeight));
    if (this.usedCodeSup.length() == 0) {
      this.usedCodeSup = chars;
    }

    if (chars.length() == 5) {
      boolean odd = true;
      int sumodd = 0;
      int sum = 0;

      for (int i = chars.length() - 1; i >= 0; --i) {
        if (odd) {
          sumodd += Integer.parseInt(String.valueOf(chars.charAt(i)));
        }
        else {
          sum += Integer.parseInt(String.valueOf(chars.charAt(i)));
        }

        odd = !odd;
      }

      sum = sumodd * 3 + sum * 9;
      String sumstr = "" + sum;
      int c = Integer.parseInt(String.valueOf(sumstr.charAt(sumstr.length() - 1)));
      String Parity = this.fiveSuplement[c];
      this.currentX = (int) ((double) this.currentX + (double) this.resolution * this.supSeparationCM);
      this.startSuplement = this.currentX;
      this.paintGuardChar(g, "bwb", "112", this.suplementTopMargin);
      String[][] set;

      for (int j = 0; j < 5; ++j) {
        set = this.setUPCEOdd;
        if (Parity.charAt(j) == 'E') {
          set = this.setUPCEEven;
        }

        int pos = this.findChar(set, "" + chars.charAt(j));
        this.paintGuardChar(g, "wbwb", set[pos][1], this.suplementTopMargin);
        if (j < 4) {
          this.paintGuardChar(g, "wb", "11", this.suplementTopMargin);
        }
      }

      this.endSuplement = this.currentX;
    }
  }

  protected void paintMAT25(Graphics g) {
    int pos;
    String codetmp = this.code;
    this.paintChar(g, "bwbwbw", "wnnnnn");

    for (int i = 0; i < codetmp.length(); ++i) {
      String c = "" + this.code.charAt(i);
      pos = this.findChar(this.set25, c);
      if (pos >= 0) {
        this.paintChar(g, "bwbwbw", this.set25[pos][1] + "n");
      }
    }

    this.paintChar(g, "bwbwbw", "wnnnnn");
  }

  protected void paintBAR39(Graphics g) {
    int pos;
    int sum = 0;
    this.paintChar(g, "bwbwbwbwb", this.set39[this.findChar(this.set39, "*")][1]);
    int inter = (int) (this.doubleI * this.doubleX * (double) this.resolution);
    if (inter == 0) {
      inter = 1;
    }

    this.currentX += inter;

    for (int i = 0; i < this.code.length(); ++i) {
      String c = "" + this.code.charAt(i);
      pos = this.findChar(this.set39, c);
      if (pos > -1) {
        sum += pos;
        this.paintChar(g, "bwbwbwbwb", this.set39[pos][1]);
        this.currentX += inter;
      }
    }

    if (this.checkCharacter) {
      pos = (int) mod(sum, 43.0D);
      this.paintChar(g, "bwbwbwbwb", this.set39[pos][1]);
      this.currentX += inter;
    }

    this.paintChar(g, "bwbwbwbwb", this.set39[this.findChar(this.set39, "*")][1]);
  }

  protected void paintCODE11(Graphics g) {
    int pos;
    int sum;
    this.paintChar(g, "bwbwbw", "nnwwnn");
    int w = 1;
    sum = 0;

    for (int i = this.code.length() - 1; i >= 0; --i) {
      sum += this.findChar(this.set11, "" + this.code.charAt(i)) * w;
      ++w;
      if (w == 11) {
        w = 1;
      }
    }

    int ch1 = (int) mod(sum, 11.0D);
    w = 2;
    sum = ch1;

    for (int i = this.code.length() - 1; i >= 0; --i) {
      sum += this.findChar(this.set11, "" + this.code.charAt(i)) * w;
      ++w;
      if (w == 10) {
        w = 1;
      }
    }

    int ch2 = (int) mod(sum, 11.0D);

    for (int i = 0; i < this.code.length(); ++i) {
      String c = "" + this.code.charAt(i);
      pos = this.findChar(this.set11, c);
      if (pos > -1) {
        this.paintChar(g, "bwbwbw", this.set11[pos][1] + "n");
      }
    }

    if (this.checkCharacter) {
      this.paintChar(g, "bwbwbw", this.set11[ch1][1] + "n");
      if (this.codeText.length() == 0) {
        this.codeText = this.code + this.set11[ch1][0];
      }

      if (this.code.length() > 10) {
        this.paintChar(g, "bwbwbw", this.set11[ch2][1] + "n");
        if (this.codeText.length() == 0) {
          this.codeText = this.codeText + this.set11[ch2][0];
        }
      }
    }

    this.paintChar(g, "bwbwb", "nnwwn");
  }

  protected void paintCODABAR(Graphics g) {
    int pos;
    int sum;
    this.paintChar(g, "bwbwbwbw", this.setCODABAR[this.findChar(this.setCODABAR, "" + this.codABarStartChar)][1] + "n");
    sum = this.findChar(this.setCODABAR, "" + this.codABarStartChar) + this.findChar(this.setCODABAR, "" + this.codABarStopChar);

    for (int i = this.code.length() - 1; i >= 0; --i) {
      sum += this.findChar(this.setCODABAR, "" + this.code.charAt(i));
    }

    int ch1 = (int) mod(sum, 16.0D);
    if (ch1 != 0) {
      ch1 = 16 - ch1;
    }

    for (int i = 0; i < this.code.length(); ++i) {
      String c = "" + this.code.charAt(i);
      pos = this.findChar(this.setCODABAR, c);
      if (pos > -1) {
        this.paintChar(g, "bwbwbwbw", this.setCODABAR[pos][1] + "n");
      }
    }

    if (this.checkCharacter) {
      if (this.codeText.length() == 0) {
        this.codeText = this.code + this.setCODABAR[ch1][0];
      }

      this.paintChar(g, "bwbwbwbw", this.setCODABAR[ch1][1] + "n");
    }

    this.paintChar(g, "bwbwbwb", this.setCODABAR[this.findChar(this.setCODABAR, "" + this.codABarStopChar)][1]);
  }

  protected int getMSIModule10(String code) {
    int sum = 0;
    StringBuilder oddNumber = new StringBuilder();
    boolean odd = true;

    for (int i = code.length() - 1; i >= 0; --i) {
      if (!odd) {
        sum += this.findChar(this.setMSI, "" + code.charAt(i));
      }

      if (odd) {
        oddNumber.insert(0, this.findChar(this.setMSI, "" + code.charAt(i)));
      }

      odd = !odd;
    }

    String newOddNumber = String.valueOf(Long.parseLong(oddNumber.toString()) * 2L);

    for (int i = newOddNumber.length() - 1; i >= 0; --i) {
      sum += this.findChar(this.setMSI, "" + oddNumber.charAt(i));
    }

    int ch1 = (int) mod(sum, 10.0D);
    if (ch1 != 0) {
      ch1 = 10 - ch1;
    }

    return ch1;
  }

  protected void paintMSI(Graphics g) {
    int pos;
    int ch11;
    int sum = 0;
    int weight = 2;
    this.paintChar(g, "bw", "wn");

    for (int i = this.code.length() - 1; i >= 0; --i) {
      sum += this.findChar(this.setMSI, "" + this.code.charAt(i)) * weight;
      ++weight;
      if (weight == 8) {
        weight = 2;
      }
    }

    ch11 = (int) mod(sum, 11.0D);
    if (ch11 != 0) {
      ch11 = 11 - ch11;
    }

    int ch1110 = this.getMSIModule10(this.code + this.setMSI[ch11][0]);
    int ch10 = this.getMSIModule10(this.code);
    int ch1010 = this.getMSIModule10(this.code + this.setMSI[ch10][0]);

    for (int i = 0; i < this.code.length(); ++i) {
      String c = "" + this.code.charAt(i);
      pos = this.findChar(this.setMSI, c);
      if (pos > -1) {
        this.paintChar(g, "bwbwbwbw", this.setMSI[pos][1]);
      }
    }

    if (this.checkCharacter) {
      if (this.msiChecksum == 0) {
        this.paintChar(g, "bwbwbwbw", this.setMSI[ch10][1]);
        this.codeText = this.code + this.setMSI[ch10][0];
      }

      if (this.msiChecksum == 1) {
        this.paintChar(g, "bwbwbwbw", this.setMSI[ch11][1]);
        this.codeText = this.code + this.setMSI[ch11][0];
      }

      if (this.msiChecksum == 3) {
        this.paintChar(g, "bwbwbwbw", this.setMSI[ch10][1]);
        this.codeText = this.code + this.setMSI[ch10][0];
        this.paintChar(g, "bwbwbwbw", this.setMSI[ch1010][1]);
        this.codeText = this.code + this.setMSI[ch1010][0];
      }

      if (this.msiChecksum == 2) {
        this.paintChar(g, "bwbwbwbw", this.setMSI[ch11][1]);
        this.codeText = this.code + this.setMSI[ch11][0];
        this.paintChar(g, "bwbwbwbw", this.setMSI[ch1110][1]);
        this.codeText = this.code + this.setMSI[ch1110][0];
      }
    }

    this.paintChar(g, "bwb", "nwn");
  }

  protected static double mod(double a, double b) {
    double f = a / b;
    double i = (double) Math.round(f);
    if (i > f) {
      --i;
    }

    return a - b * i;
  }

  protected void paintBAR39Ext(Graphics g) {
    int pos;
    int sum = 0;
    this.paintChar(g, "bwbwbwbwb", this.set39[this.findChar(this.set39, "*")][1]);
    int inter = (int) (this.doubleI * this.doubleX * (double) this.resolution);
    if (inter == 0) {
      inter = 1;
    }

    this.currentX += inter;

    for (int i = 0; i < this.code.length(); ++i) {
      byte b = (byte) this.code.charAt(i);
      if (b <= 128) {
        String encoded = this.set39Ext[b];

        for (int j = 0; j < encoded.length(); ++j) {
          String c = "" + encoded.charAt(j);
          pos = this.findChar(this.set39, c);
          if (pos > -1) {
            sum += pos;
            this.paintChar(g, "bwbwbwbwb", this.set39[pos][1]);
            this.currentX += inter;
          }
        }
      }
    }

    if (this.checkCharacter) {
      pos = (int) mod(sum, 43.0D);
      this.paintChar(g, "bwbwbwbwb", this.set39[pos][1]);
      this.currentX += inter;
      if (this.codeText.length() == 0) {
        this.codeText = this.code + "" + this.set39[pos][0];
      }
    }

    this.paintChar(g, "bwbwbwbwb", this.set39[this.findChar(this.set39, "*")][1]);
  }

  protected void paintBAR93(Graphics g) {
    int pos;
    int sum = 0;
    int ch2;
    int ch1;
    this.paintChar(g, "bwbwbw", "111141");

    for (int i = 0; i < this.code.length(); ++i) {
      String c = "" + this.code.charAt(i);
      pos = this.findChar(this.set93, c);
      if (pos > -1) {
        sum += pos;
        this.paintChar(g, "bwbwbw", this.set93[pos][1]);
      }
    }

    int w = 1;
    sum = 0;

    for (int i = this.code.length() - 1; i >= 0; --i) {
      sum += this.findChar(this.set93, "" + this.code.charAt(i)) * w;
      ++w;
      if (w == 21) {
        w = 1;
      }
    }

    ch1 = (int) mod(sum, 47.0D);
    w = 2;
    sum = ch1;

    for (int i = this.code.length() - 1; i >= 0; --i) {
      sum += this.findChar(this.set93, "" + this.code.charAt(i)) * w;
      ++w;
      if (w == 16) {
        w = 1;
      }
    }

    ch2 = (int) mod(sum, 47.0D);
    if (this.checkCharacter) {
      this.paintChar(g, "bwbwbw", this.set93[ch1][1]);
      this.paintChar(g, "bwbwbw", this.set93[ch2][1]);
      if (this.codeText.length() == 0) {
        this.codeText = this.code + this.set93[ch1][0].charAt(0) + this.set93[ch2][0].charAt(0);
      }
    }

    this.paintChar(g, "bwbwbwb", "1111411");
  }

  protected void paintBAR93Ext(Graphics g) {
    int pos;
    int sum = 0;
    int ch2;
    int ch1;
    this.paintChar(g, "bwbwbw", "111141");

    String c;
    String encoded;
    byte b;
    for (int i = 0; i < this.code.length(); ++i) {
      b = (byte) this.code.charAt(i);
      if (b <= 128) {
        encoded = this.set93Ext[b];
        if (encoded.length() == 3) {
          c = "" + encoded.charAt(0) + encoded.charAt(1);
          pos = this.findChar(this.set93, c);
          this.paintChar(g, "bwbwbw", this.set93[pos][1]);
          c = "" + encoded.charAt(2);
        }
        else {
          c = "" + encoded.charAt(0);
        }

        pos = this.findChar(this.set93, c);
        sum += pos;
        this.paintChar(g, "bwbwbw", this.set93[pos][1]);
      }
    }

    int w = 1;
    sum = 0;

    for (int i = this.code.length() - 1; i >= 0; --i) {
      b = (byte) this.code.charAt(i);
      if (b <= 128) {
        encoded = this.set93Ext[b];
        if (encoded.length() == 3) {
          c = "" + encoded.charAt(0) + encoded.charAt(1);
          pos = this.findChar(this.set93, c);
          sum += pos * (w + 1);
          c = "" + encoded.charAt(2);
          pos = this.findChar(this.set93, c);
          sum += pos * w;
          ++w;
          if (w == 21) {
            w = 1;
          }

          ++w;
          if (w == 21) {
            w = 1;
          }
        }
        else {
          c = "" + encoded.charAt(0);
          pos = this.findChar(this.set93, c);
          sum += pos * w;
          ++w;
          if (w == 21) {
            w = 1;
          }
        }
      }
    }

    ch1 = (int) mod(sum, 47.0D);
    w = 2;
    sum = ch1;

    for (int i = this.code.length() - 1; i >= 0; --i) {
      b = (byte) this.code.charAt(i);
      if (b <= 128) {
        encoded = this.set93Ext[b];
        if (encoded.length() == 3) {
          c = "" + encoded.charAt(0) + encoded.charAt(1);
          pos = this.findChar(this.set93, c);
          sum += pos * (w + 1);
          c = "" + encoded.charAt(2);
          pos = this.findChar(this.set93, c);
          sum += pos * w;
          ++w;
          if (w == 16) {
            w = 1;
          }

          ++w;
          if (w == 16) {
            w = 1;
          }
        }
        else {
          c = "" + encoded.charAt(0);
          pos = this.findChar(this.set93, c);
          sum += pos * w;
          ++w;
          if (w == 16) {
            w = 1;
          }
        }
      }
    }

    ch2 = (int) mod(sum, 47.0D);
    if (this.checkCharacter) {
      this.paintChar(g, "bwbwbw", this.set93[ch1][1]);
      this.paintChar(g, "bwbwbw", this.set93[ch2][1]);
      if (this.codeText.length() == 0) {
        this.codeText = this.code + this.set93[ch1][0].charAt(0) + this.set93[ch2][0].charAt(0);
      }
    }

    this.paintChar(g, "bwbwbwb", "1111411");
  }

  protected void paintChar(Graphics g, String patternColor, String patternBars) {
    this.paintChar2(g, patternColor, patternBars, 0);
  }

  protected void paintChar2(Graphics g, String patternColor, String patternBars, int bartopmargin) {
    for (int i = 0; i < patternColor.length(); ++i) {
      char cColor = patternColor.charAt(i);
      char cBar = patternBars.charAt(i);
      if (cBar == 'n') {
        this.addBar(g, this.narrowBarPixels, cColor == 'b', bartopmargin);
      }

      if (cBar == 'w') {
        this.addBar(g, this.widthBarPixels, cColor == 'b', bartopmargin);
      }

      if (cBar == '1') {
        this.addBar(g, this.narrowBarPixels, cColor == 'b', bartopmargin);
      }

      if (cBar == '2') {
        this.addBar(g, (int) (this.narrowBarCM * (double) this.resolution * 2.0D), cColor == 'b', bartopmargin);
      }

      if (cBar == '3') {
        this.addBar(g, (int) (this.narrowBarCM * (double) this.resolution * 3.0D), cColor == 'b', bartopmargin);
      }

      if (cBar == '4') {
        this.addBar(g, (int) (this.narrowBarCM * (double) this.resolution * 4.0D), cColor == 'b', bartopmargin);
      }
    }
  }

  protected void paintGuardChar(Graphics g, String patternColor, String patternBars, int bartopMargin) {
    if (this.textFont != null && this.guardBars) {
      g.setFont(this.textFont);
      this.extraHeight = g.getFontMetrics().getHeight();
    }

    this.paintChar2(g, patternColor, patternBars, bartopMargin);
    this.extraHeight = 0;
  }

  protected void calculateSizes() {
    int c = this.code.length();
    this.narrowBarCM = this.doubleX;
    this.widthBarCM = this.doubleX * this.doubleN;
    if (this.barType == 2) {
      if (mod(c, 2.0D) == 0.0D && this.checkCharacter) {
        ++c;
      }

      if (mod(c, 2.0D) == 1.0D && !this.checkCharacter) {
        ++c;
      }

      if (this.checkCharacter) {
        ++c;
      }

      this.doubleL = (c / 2D) * (3.0D + 2.0D * this.doubleN) * this.doubleX + 7.0D * this.doubleX;
    }

    if (this.barType == 6) {
      if (this.checkCharacter) {
        ++c;
      }

      this.doubleL = (double) (c * 7) * this.doubleX + 11.0D * this.doubleX;
    }

    if (this.barType == 10) {
      this.doubleL = (double) (c * 7) * this.doubleX + 11.0D * this.doubleX;
    }

    if (this.barType == 11) {
      this.doubleL = (double) (c * 7) * this.doubleX + 11.0D * this.doubleX;
    }

    if (this.barType == 13 || this.barType == 16) {
      if (this.checkCharacter) {
        ++c;
      }

      if (this.code128Set == 'C') {
        this.doubleL = (double) (11 * c + 35) * this.doubleX;
      }
      else {
        this.doubleL = (5.5D * (double) c + 35.0D) * this.doubleX;
      }
    }

    if (this.barType == 12) {
      this.doubleL = 42.0D * this.doubleX + 9.0D * this.doubleX;
    }

    if (this.barType == 7) {
      if (this.checkCharacter) {
        ++c;
      }

      this.doubleL = (double) c * (3.0D + 2.0D * this.doubleN) * this.doubleX + 7.0D * this.doubleX;
    }

    if (this.barType == 8) {
      if (this.checkCharacter) {
        ++c;
      }

      this.doubleL = (double) c * (3.0D + 2.0D * this.doubleN) * this.doubleX + 7.0D * this.doubleX;
    }

    if (this.barType == 5) {
      if (this.checkCharacter) {
        ++c;
      }

      this.doubleL = (double) c * (4.0D + 4.0D * this.doubleN) * this.doubleX + (1.0D + this.doubleN) * this.doubleX + (2.0D + this.doubleN) * this.doubleX;
    }

    if (this.barType == 4) {
      if (this.checkCharacter) {
        ++c;
      }

      this.doubleL = (double) (c + 2) * (4.0D + 3.0D * this.doubleN) * this.doubleX;
    }

    if (this.barType == 3) {
      if (this.checkCharacter || this.code.length() > 10) {
        ++c;
      }

      this.doubleL = (double) (c + 2 + 1) * (3.0D + 2.0D * this.doubleN) * this.doubleX;
    }

    if (this.barType == 15) {
      if (this.checkCharacter) {
        ++c;
      }

      this.doubleL = this.doubleX * 10.0D;
    }

    if (this.barType == 0) {
      if (this.checkCharacter) {
        ++c;
      }

      this.doubleL = (double) (c + 2) * (3.0D * this.doubleN + 6.0D) * this.doubleX + (double) (c + 1) * this.doubleI * this.doubleX;
    }

    byte b;
    String encoded;
    int j;
    if (this.barType == 1) {
      c = 0;
      if (this.checkCharacter) {
        ++c;
      }

      for (j = 0; j < this.code.length(); ++j) {
        b = (byte) this.code.charAt(j);
        if (b <= 128) {
          encoded = this.set39Ext[b];
          c += encoded.length();
        }
      }

      this.doubleL = (double) (c + 2) * (3.0D * this.doubleN + 6.0D) * this.doubleX + (double) (c + 1) * this.doubleI * this.doubleX;
    }

    if (this.barType == 9 || this.barType == 14) {
      c = 0;
      if (this.checkCharacter) {
        ++c;
      }

      for (j = 0; j < this.code.length(); ++j) {
        b = (byte) this.code.charAt(j);
        if (b <= 128) {
          encoded = this.set39Ext[b];
          if (encoded.length() == 1) {
            ++c;
          }
          else {
            c += 2;
          }
        }
      }

      this.doubleL = (double) (c + 2) * 9.0D * this.doubleX + (double) (c + 1) * this.doubleI * this.doubleX;
    }

    if (this.barHeightCM == 0.0D) {
      this.barHeightCM = this.doubleL * this.doubleH;
      if (this.barHeightCM < 0.625D) {
        this.barHeightCM = 0.625D;
      }
    }

    if (this.barHeightCM != 0.0D) {
      this.barHeightPixels = (int) (this.barHeightCM * (double) this.resolution);
    }

    if (this.narrowBarCM != 0.0D) {
      this.narrowBarPixels = (int) (this.narrowBarCM * (double) this.resolution);
    }

    if (this.widthBarCM != 0.0D) {
      this.widthBarPixels = (int) (this.widthBarCM * (double) this.resolution);
    }

    if (this.narrowBarPixels <= 0) {
      this.narrowBarPixels = 1;
    }

    if (this.widthBarPixels <= 1) {
      this.widthBarPixels = 2;
    }
  }

  @Override
  public void paint(Graphics g2) {
    Graphics g = g2;
    Image im = null;
    if (this.rotate != 0) {
      String v = System.getProperty("java.version");
      if (v.indexOf("1.1") != 0) {
        RImageCreator imc = new RImageCreator();
        im = imc.getImage(this.getSize().width, this.getSize().height);
        g = imc.getGraphics();
      }
      else {
        if (this.getSize().width > this.getSize().height) {
          im = this.createImage(this.getSize().width, this.getSize().width);
        }
        else {
          im = this.createImage(this.getSize().height, this.getSize().height);
        }

        g = im.getGraphics();
      }
    }

    g2.setColor(this.backColor);
    g2.fillRect(0, 0, this.getSize().width, this.getSize().height);
    this.paintBasis(g);
    if (this.rotate != 0) {
      int maxw = this.currentX + this.leftMarginPixels;
      int maxh = this.currentY + this.topMarginPixels;
      Image imRotated = this.rotate(im, this.rotate, maxw, maxh);
      if (imRotated == null) {
        g2.drawImage(im, 0, 0, null);
      }
      else {
        g2.drawImage(imRotated, 0, 0, null);
      }
    }
  }

  protected void paintBasis(Graphics g) {
    this.codeText = "";
    this.calculateSizes();
    this.topMarginPixels = (int) (this.topMarginCM * (double) this.resolution);
    this.leftMarginPixels = (int) (this.leftMarginCM * (double) this.resolution);
    this.currentX = this.leftMarginPixels;
    g.setColor(this.backColor);
    int w = this.getSize().width;
    int h = this.getSize().height;
    int m = w;
    if (h > w) {
      m = h;
    }

    g.fillRect(0, 0, m, m);
    this.endOfCode = 0;
    String tmpCode = this.code;
    if (this.processTilde) {
      this.code = this.applyTilde(this.code);
    }

    if (this.barType == 3) {
      this.paintCODE11(g);
    }

    if (this.barType == 5) {
      this.paintMSI(g);
    }

    if (this.barType == 4) {
      this.paintCODABAR(g);
    }

    if (this.barType == 0) {
      this.paintBAR39(g);
    }

    if (this.barType == 1) {
      this.paintBAR39Ext(g);
    }

    if (this.barType == 2) {
      this.paintInterleaved25(g);
    }

    if (this.barType == 9) {
      this.paintBAR93(g);
    }

    if (this.barType == 11) {
      this.paintEAN8(g);
    }

    if (this.barType == 10) {
      this.paintEAN13(g);
    }

    if (this.barType == 6) {
      this.paintUPCA(g);
    }

    if (this.barType == 12) {
      this.paintUPCE(g);
    }

    if (this.barType == 13) {
      this.paintCode128(g);
    }

    if (this.barType == 16) {
      this.paintEAN128(g);
    }

    if (this.barType == 14) {
      this.paintBAR93Ext(g);
    }

    if (this.barType == 7) {
      this.paintIND25(g);
    }

    if (this.barType == 8) {
      this.paintMAT25(g);
    }

    if (this.barType == 15) {
      this.paintPOSTNET(g);
    }

    this.code = tmpCode;
    if (this.endOfCode == 0) {
      this.endOfCode = this.currentX;
    }

    if (this.codeText.length() == 0) {
      this.codeText = this.code;
    }

    this.currentY = this.barHeightPixels + this.topMarginPixels;
    if (this.textFont != null) {
      g.setColor(this.fontColor);
      g.setFont(this.textFont);
      int textH = g.getFontMetrics().getHeight();
      int charW = g.getFontMetrics().stringWidth("X");
      int toCenterX;
      if ((this.upceanSupplement2 || this.upceanSupplement5) && (this.barType == 11 || this.barType == 6 || this.barType == 12 || this.barType == 10)) {
        toCenterX = (this.endSuplement - this.startSuplement - g.getFontMetrics().stringWidth(this.usedCodeSup)) / 2;
        if (toCenterX < 0) {
          toCenterX = 0;
        }

        g.drawString(this.usedCodeSup, this.startSuplement + toCenterX, this.topMarginPixels + this.suplementTopMargin - 2);
      }

      if (this.barType == 15) {
        toCenterX = (this.endOfCode - this.leftMarginPixels - g.getFontMetrics().stringWidth(this.codeText)) / 2;
        if (toCenterX < 0) {
          toCenterX = 0;
        }

        g.drawString(this.codeText, this.leftMarginPixels + toCenterX, (int) (this.postnetHeightTallBar * (double) this.resolution + (double) textH + 1.0D + (double) this.topMarginPixels));
        this.currentY = this.barHeightPixels + textH + 1 + this.topMarginPixels;
        return;
      }

      if (this.barType == 10 && this.guardBars && this.codeText.length() >= 13) {
        g.drawString(this.codeText.substring(0, 1), this.leftMarginPixels - charW, this.barHeightPixels + textH + 1 + this.topMarginPixels);
        toCenterX = (this.centerGuardBarStart - this.leftGuardBar - g.getFontMetrics().stringWidth(this.codeText.substring(1, 7))) / 2;
        if (toCenterX < 0) {
          toCenterX = 0;
        }

        g.drawString(this.codeText.substring(1, 7), this.leftGuardBar + toCenterX, this.barHeightPixels + textH + 1 + this.topMarginPixels);
        toCenterX = (this.rightGuardBar - this.centerGuardBarEnd - g.getFontMetrics().stringWidth(this.codeText.substring(7, 13))) / 2;
        if (toCenterX < 0) {
          toCenterX = 0;
        }

        g.drawString(this.codeText.substring(7, 13), this.centerGuardBarEnd + toCenterX, this.barHeightPixels + textH + 1 + this.topMarginPixels);
        this.currentY = this.barHeightPixels + textH + 1 + this.topMarginPixels;
        return;
      }

      if (this.barType == 6 && this.guardBars && this.codeText.length() >= 12) {
        g.drawString(this.codeText.substring(0, 1), this.leftMarginPixels - charW, this.barHeightPixels + textH + 1 + this.topMarginPixels);
        toCenterX = (this.centerGuardBarStart - this.leftGuardBar - g.getFontMetrics().stringWidth(this.codeText.substring(1, 6))) / 2;
        if (toCenterX < 0) {
          toCenterX = 0;
        }

        g.drawString(this.codeText.substring(1, 6), this.leftGuardBar + toCenterX, this.barHeightPixels + textH + 1 + this.topMarginPixels);
        toCenterX = (this.rightGuardBar - this.centerGuardBarEnd - g.getFontMetrics().stringWidth(this.codeText.substring(6, 11))) / 2;
        if (toCenterX < 0) {
          toCenterX = 0;
        }

        g.drawString(this.codeText.substring(6, 11), this.centerGuardBarEnd + toCenterX, this.barHeightPixels + textH + 1 + this.topMarginPixels);
        g.drawString(this.codeText.substring(11, 12), this.endOfCode + 3, this.barHeightPixels + textH + 1 + this.topMarginPixels);
        this.currentY = this.barHeightPixels + textH + 1 + this.topMarginPixels;
        return;
      }

      if (this.barType == 11 && this.guardBars && this.codeText.length() >= 8) {
        toCenterX = (this.centerGuardBarStart - this.leftGuardBar - g.getFontMetrics().stringWidth(this.codeText.substring(0, 4))) / 2;
        if (toCenterX < 0) {
          toCenterX = 0;
        }

        g.drawString(this.codeText.substring(0, 4), this.leftGuardBar + toCenterX, this.barHeightPixels + textH + 1 + this.topMarginPixels);
        toCenterX = (this.rightGuardBar - this.centerGuardBarEnd - g.getFontMetrics().stringWidth(this.codeText.substring(4, 8))) / 2;
        if (toCenterX < 0) {
          toCenterX = 0;
        }

        g.drawString(this.codeText.substring(4, 8), this.centerGuardBarEnd + toCenterX, this.barHeightPixels + textH + 1 + this.topMarginPixels);
        this.currentY = this.barHeightPixels + textH + 1 + this.topMarginPixels;
        return;
      }

      if (this.barType == 12 && this.guardBars) {
        g.drawString(this.code.substring(0, 1), this.leftMarginPixels - charW, this.barHeightPixels + textH + 1 + this.topMarginPixels);
        toCenterX = (this.rightGuardBar - this.leftGuardBar - g.getFontMetrics().stringWidth(this.codeText)) / 2;
        if (toCenterX < 0) {
          toCenterX = 0;
        }

        g.drawString(this.codeText, this.leftGuardBar + toCenterX, this.barHeightPixels + textH + 1 + this.topMarginPixels);
        this.currentY = this.barHeightPixels + textH + 1 + this.topMarginPixels;
        return;
      }

      toCenterX = (this.endOfCode - this.leftMarginPixels - g.getFontMetrics().stringWidth(this.codeText)) / 2;
      if (toCenterX < 0) {
        toCenterX = 0;
      }

      if (!BarCode.TEXT_ON_TOP) {
        g.drawString(this.codeText, this.leftMarginPixels + toCenterX, this.barHeightPixels + textH + 1 + this.topMarginPixels);
      }
      else {
        g.drawString(this.codeText, this.leftMarginPixels + toCenterX, textH + 4);
      }

      this.currentY = this.barHeightPixels + textH + 1 + this.topMarginPixels;
    }
  }

  protected Image rotate(Image im, int angle, int maxw, int maxh) {
    int w = im.getWidth(null);
    int h = im.getHeight(null);
    if (maxw > w) {
      maxw = w;
    }

    if (maxh > h) {
      maxh = h;
    }

    int[] pixels = new int[w * h];
    int[] pixels2 = new int[maxw * maxh];
    PixelGrabber pg = new PixelGrabber(im, 0, 0, w, h, pixels, 0, w);

    try {
      pg.grabPixels();
    }
    catch (InterruptedException var12) {
      log.error("interrupted waiting for pixels!", var12);
      return null;
    }

    if ((pg.getStatus() & 128) != 0) {
      log.error("image fetch aborted or errored");
      return null;
    }
    else {
      int i;
      int j;
      if (angle == 90) {
        for (i = 0; i < maxw; ++i) {
          for (j = 0; j < maxh; ++j) {
            pixels2[maxh * (maxw - (i + 1)) + j] = pixels[j * w + i];
          }
        }

        return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(maxh, maxw, pixels2, 0, maxh));
      }
      else if (angle == 180) {
        for (i = 0; i < maxw; ++i) {
          for (j = 0; j < maxh; ++j) {
            pixels2[(maxh - (j + 1)) * maxw + (maxw - (i + 1))] = pixels[j * w + i];
          }
        }

        return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(maxw, maxh, pixels2, 0, maxw));
      }
      else if (angle != 270) {
        return null;
      }
      else {
        for (i = 0; i < maxw; ++i) {
          for (j = 0; j < maxh; ++j) {
            pixels2[maxh * i + (maxh - (j + 1))] = pixels[j * w + i];
          }
        }

        return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(maxh, maxw, pixels2, 0, maxh));
      }
    }
  }

  private String applyTilde(String code) {
    int c;
    int longi = code.length();
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < longi; ++i) {
      c = code.charAt(i);
      if (c == '~') {
        if (i < longi - 1) {
          char nextc = code.charAt(i + 1);
          if (nextc == '~') {
            result.append('~');
            ++i;
          }
          else if (i < longi - 3) {
            String ascString = code.substring(i + 2, i + 5);

            int asc;
            try {
              asc = new Integer(ascString);
            }
            catch (Exception var11) {
              asc = 0;
            }

            if (asc > 255) {
              asc = 255;
            }

            result.append((char) asc);
            i += 4;
          }
        }
      }
      else {
        result.append((char) c);
      }
    }

    return result.toString();
  }

  public String getEAN128Text(String code) {
    int len = code.length();
    StringBuilder result = new StringBuilder();
    StringBuilder newCode = new StringBuilder();
    int i = 0;
    String d2 = "";

    while (i < len) {
      d2 = code.substring(i, i + 2);
      int aiLen = this.getAiLen(d2);
      if (aiLen == 0) {
        log.error("Unknown EAN128 App.Id. " + d2);
        return code;
      }

      int fixedLen = this.getFixedFieldLen(Integer.parseInt(d2));

      int j;
      for (j = i; j < len && code.charAt(j) != ' ' && code.charAt(j) != 202; ++j) {
      }

      int fieldLen = j - i;

      int dataLen = fieldLen - aiLen;
      newCode.append(code, i, i + fieldLen);
      if (result.length() > 0) {
        result.append(" ");
      }

      result.append("(").append(code, i, i + aiLen).append(")");
      result.append(code, i + aiLen, i + aiLen + dataLen);
      i += fieldLen;
      if (i < len && (code.charAt(i) == ' ' || code.charAt(j) == 202)) {
        ++i;
      }

      if (fixedLen == 0 && i < len) {
        newCode.append('Ê');
      }
    }

    return result.toString();
  }

  public int getFixedFieldLen(int i) {
    if (i == 0) {
      return 20;
    }
    else if (i <= 3) {
      return 16;
    }
    else if (i == 4) {
      return 18;
    }
    else if (i <= 10) {
      return 0;
    }
    else if (i <= 19) {
      return 8;
    }
    else if (i == 20) {
      return 4;
    }
    else if (i <= 30) {
      return 0;
    }
    else if (i <= 36) {
      return 10;
    }
    else {
      return i == 41 ? 16 : 0;
    }
  }

  public int getAiLen(String d2) {
    int id2 = Integer.parseInt(d2.substring(0, 2));
    switch (d2.charAt(0)) {
      case '0':
        if (id2 >= 0 && id2 <= 4) {
          return 2;
        }

        return 0;
      case '1':
        return 2;
      case '2':
        if (id2 >= 20 && id2 <= 22) {
          return 2;
        }
        else {
          if (id2 >= 23 && id2 <= 25) {
            return 3;
          }

          return 0;
        }
      case '3':
        if (id2 == 38) {
          return 0;
        }
        else {
          if (id2 != 30 && id2 != 37) {
            return 4;
          }

          return 2;
        }
      case '4':
        if (id2 >= 40 && id2 <= 42) {
          return 3;
        }

        return 0;
      case '5':
      case '6':
      case '7':
        if (id2 == 70) {
          return 4;
        }

        return 0;
      case '8':
        if (id2 != 80 && id2 != 81) {
          return 0;
        }

        return 4;
      case '9':
        return 2;

      default:
        return 0;
    }
  }
}
