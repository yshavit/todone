package com.yuvalshavit.todone.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

public class FileBasedDao implements TodoneDao {

  private static final Charset CHARSET = StandardCharsets.UTF_8;
  private static final String nl = System.getProperty("line.separator", "\n");
  private final File file;

  public FileBasedDao() {
    AppDirs appDirs = AppDirsFactory.getInstance();
    File dataDir = new File(appDirs.getUserDataDir("com.yuvalshavit.todone", "0.1", "Yuval Shavit"));
    if (!dataDir.exists()) {
      if (!dataDir.mkdirs()) {
        throw new RuntimeException("can't create data directory: " + dataDir);
      }
    } else if (!dataDir.isDirectory()) {
      throw new RuntimeException("can't create data directory: " + dataDir);
    }
    file = new File(dataDir, "accomplishments.txt");
    System.err.printf("using %s%n", file);
  }

  @Override
  public void add(Accomplishment accomplishment) {
    try (FileOutputStream fos = new FileOutputStream(file, true);
         Writer writer = new OutputStreamWriter(fos, CHARSET))
    {
      writer
        .append(String.valueOf(accomplishment.getTimestamp() / 1000))
        .append(' ')
        .append(escape(accomplishment.getText()))
        .append(nl);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Iterable<Accomplishment> fetchAll() {
    List<Accomplishment> accomplishments = new ArrayList<>();
    try (FileInputStream fis = new FileInputStream(file);
         Reader reader = new InputStreamReader(fis, CHARSET);
         BufferedReader lineReader = new BufferedReader(reader))
    {
      lineReader.lines().forEach(line -> {
        String[] splits = line.split("\\s", 2);
        if (splits.length != 2) {
          System.err.printf("error parsing line: %s", line);
        } else {
          long epochSeconds;
          try {
            epochSeconds = Long.parseLong(splits[0]);
          } catch (NumberFormatException e) {
            System.err.printf("error parsing line: %s", line);
            return;
          }
          accomplishments.add(new Accomplishment(epochSeconds * 1000, unEscape(splits[1])));
        }
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return accomplishments;
  }

  private String escape(String plain) {
    return plain.replaceAll("[\\r\\n]", ""); // TODO find a way to escape these?
  }

  private String unEscape(String escaped) {
    return escaped; // TODO lossy, but whatever
  }

  public static void main(String[] args) {
    DummyDao source = DummyDao.prePopulated();
    FileBasedDao destination = new FileBasedDao();
    for (Accomplishment accomplishment : source.fetchAll()) {
      destination.add(accomplishment);
    }
  }

}
