/* Generated By:JavaCC: Do not edit this line. ParserShips.java */
package pt.rmartins.battleships.parser.ships;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.lang.StringBuilder;

import android.util.Log;

import pt.rmartins.battleships.objects.ShipClass;
import pt.rmartins.battleships.objects.Coordinate;
import pt.rmartins.battleships.utilities.LanguageClass;

public class ParserShips implements ParserShipsConstants {

        public static void parseShips(String s)
        {
                parseShips(new ByteArrayInputStream(s.getBytes()));
        }

        public static void parseShips(InputStream in)
        {
                ParserShips parser = new ParserShips(in);
                try {
                        parser.main();
                }catch(Exception e){
                    Log.e("Error reading ships file", e.getMessage());
                }
        }

  final public void main() throws ParseException {
  List<Locale> languages; int version; Token t;
    languages = new ArrayList<Locale>();
    version = 1;
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case VERSION:
        jj_consume_token(VERSION);
        jj_consume_token(DDOT);
        t = jj_consume_token(NUM);
        jj_consume_token(SEMICOLON);
                                                     version = Integer.parseInt(t.image);
        break;
      case LANGUAGES:
        jj_consume_token(LANGUAGES);
        jj_consume_token(DDOT);
        t = jj_consume_token(ID);
                                        languages.add(LanguageClass.getLocale(t.image));
        label_2:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case COMMA:
            ;
            break;
          default:
            jj_la1[0] = jj_gen;
            break label_2;
          }
          jj_consume_token(COMMA);
          t = jj_consume_token(ID);
                                            languages.add(LanguageClass.getLocale(t.image));
        }
        jj_consume_token(SEMICOLON);
        break;
      default:
        jj_la1[1] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case VERSION:
      case LANGUAGES:
        ;
        break;
      default:
        jj_la1[2] = jj_gen;
        break label_1;
      }
    }
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NUM:
      case ID:
        ;
        break;
      default:
        jj_la1[3] = jj_gen;
        break label_3;
      }
      readShip(languages);
    }
    {if (true) return;}
  }

  final public void readShip(List<Locale> languages) throws ParseException {
  Token t; StringBuilder sb; List<Coordinate> parts; Map<String, String> names; int index;
    names = new TreeMap<String, String>(); index = 0;
      sb = new StringBuilder();
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ID:
        t = jj_consume_token(ID);
        break;
      case NUM:
        t = jj_consume_token(NUM);
        break;
      default:
        jj_la1[4] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
                                     sb.append(t.image + " ");
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NUM:
      case ID:
        ;
        break;
      default:
        jj_la1[5] = jj_gen;
        break label_4;
      }
    }
      names.put(languages.get(index).getLanguage(), sb.toString().trim());
      index++;
    label_5:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMMA:
        ;
        break;
      default:
        jj_la1[6] = jj_gen;
        break label_5;
      }
      jj_consume_token(COMMA);
      sb = new StringBuilder();
      label_6:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case ID:
          t = jj_consume_token(ID);
          break;
        case NUM:
          t = jj_consume_token(NUM);
          break;
        default:
          jj_la1[7] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
                                     sb.append(t.image + " ");
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case NUM:
        case ID:
          ;
          break;
        default:
          jj_la1[8] = jj_gen;
          break label_6;
        }
      }
      names.put(languages.get(index).getLanguage(), sb.toString().trim());
      index++;
    }
    jj_consume_token(SEMICOLON);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NUM:
      jj_consume_token(NUM);
      break;
    case HEX:
      jj_consume_token(HEX);
      break;
    default:
      jj_la1[9] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    jj_consume_token(SEMICOLON);
    parts = readCoordinates();
    if(!parts.isEmpty())
      ShipClass.createNewShip(names, parts);
  }

  final public List<Coordinate > readCoordinates() throws ParseException {
  Token x, y; List<Coordinate> list;
    list = new ArrayList<Coordinate>();
    label_7:
    while (true) {
      x = jj_consume_token(NUM);
      jj_consume_token(COMMA);
      y = jj_consume_token(NUM);
                                        list.add(new Coordinate(Integer.parseInt(x.image), Integer.parseInt(y.image)));
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NUM:
        ;
        break;
      default:
        jj_la1[10] = jj_gen;
        break label_7;
      }
    }
    jj_consume_token(SEMICOLON);
    {if (true) return list;}
    throw new Error("Missing return statement in function");
  }

  /** Generated Token Manager. */
  public ParserShipsTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[11];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x40,0x300,0x300,0xc00,0xc00,0xc00,0x40,0xc00,0xc00,0x1400,0x400,};
   }

  /** Constructor with InputStream. */
  public ParserShips(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public ParserShips(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new ParserShipsTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 11; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 11; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public ParserShips(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new ParserShipsTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 11; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 11; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public ParserShips(ParserShipsTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 11; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(ParserShipsTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 11; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[16];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 11; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 16; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

}
