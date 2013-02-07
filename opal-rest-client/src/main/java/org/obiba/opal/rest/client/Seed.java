package org.obiba.opal.rest.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.opal.rest.client.magma.OpalJavaClient;
import org.obiba.opal.rest.client.magma.UriBuilder;
import org.obiba.opal.web.model.Commands.ImportCommandOptionsDto;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.CsvDatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.CsvDatasourceTableBundleDto;
import org.obiba.opal.web.model.Magma.DatasourceDto;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.ExcelDatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.FileViewDto;
import org.obiba.opal.web.model.Magma.FsDatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.HibernateDatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.obiba.opal.web.model.Opal.FileDto;
import org.obiba.opal.web.model.Opal.FunctionalUnitDto;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class Seed {

  private final Seeder[] seeders = new Seeder[] { new FileSystemSeeder(), new FunctionalUnitsSeeder(),
      new ReportsSeeder(), new DatasourcesSeeder(), new OpalXmlImportSeeder(), new CsvImportSeeder(),
      new ViewsSeeder() };

  private final OpalJavaClient opalClient;

  private final File seedFile;

  private final JSONObject seed;

  public Seed(File seedFile, String uri, String username, String password)
      throws URISyntaxException, IOException, JSONException {
    this.seedFile = seedFile;
    String seedStr = readFully();
    seed = new JSONObject(seedStr);
    opalClient = new OpalJavaClient(uri + "/ws", username, password);
  }

  private String readFully() throws IOException {
    StringBuilder builder = new StringBuilder();
    BufferedReader br = new BufferedReader(new FileReader(seedFile));
    try {
      String line = br.readLine();
      while(line != null) {
        builder.append(line).append('\n');
        line = br.readLine();
      }
      return builder.toString();
    } finally {
      br.close();
    }
  }

  public void seed() {
    for(Seeder s : seeders) {
      try {
        s.seed(seed);
      } catch(JSONException e) {
        System.err.println("Error seeding Opal instance: " + e.getMessage());
        break;
      } catch(IOException e) {
        System.err.println("Error seeding Opal instance: " + e.getMessage());
        break;
      }
    }
    System.out.println("All done.");
  }

  private DatasourceDto createTransientDatasource(@SuppressWarnings("TypeMayBeWeakened") DatasourceFactoryDto dto)
      throws IOException {
    HttpResponse r = opalClient.post(opalClient.newUri().segment("transient-datasources").build(), dto);
    if(r.getStatusLine().getStatusCode() != SC_CREATED) {
      throw new IOException("Could not create datasource: " + r.getStatusLine().getReasonPhrase());
    }
    return DatasourceDto.newBuilder().mergeFrom(r.getEntity().getContent()).build();
  }

  private interface Seeder {
    void seed(JSONObject seed) throws JSONException, IOException;
  }

  private abstract static class ArraySeeder implements Seeder {

    protected abstract String getKey();

    protected abstract void onSeed(JSONArray seed) throws JSONException, IOException;

    @Override
    public void seed(JSONObject seed) throws JSONException, IOException {
      JSONArray value = seed.optJSONArray(getKey());
      if(value != null) {
        onSeed(value);
      }
    }
  }

  private class DatasourcesSeeder extends ArraySeeder {

    @Override
    protected void onSeed(JSONArray seed) throws JSONException, IOException {
      for(int i = 0; i < seed.length(); i++) {
        JSONObject jsonDs = seed.getJSONObject(i);

        String name = jsonDs.getString("name");
        HttpResponse r = opalClient.get(opalClient.newUri().segment("datasource", name).build());
        r.getEntity().consumeContent();

        if(r.getStatusLine().getStatusCode() == SC_NOT_FOUND) {
          System.out.println("Creating datasource " + name);
          ignore(opalClient.post(opalClient.newUri().segment("datasources").build(),
              DatasourceFactoryDto.newBuilder().setName(name).setExtension(HibernateDatasourceFactoryDto.params,
                  HibernateDatasourceFactoryDto.newBuilder().setKey(false).build()).build()));
        }

      }
    }

    @Override
    protected String getKey() {
      return "datasources";
    }

  }

  private class FunctionalUnitsSeeder extends ArraySeeder {

    @Override
    protected void onSeed(JSONArray seed) throws JSONException, IOException {
      for(int i = 0; i < seed.length(); i++) {
        JSONObject jsonDs = seed.getJSONObject(i);

        String name = jsonDs.getString("name");
        HttpResponse r = opalClient.get(opalClient.newUri().segment("functional-unit", name).build());
        r.getEntity().consumeContent();
        if(r.getStatusLine().getStatusCode() == SC_NOT_FOUND) {
          System.out.println("Creating unit " + name);
          ignore(opalClient.post(opalClient.newUri().segment("functional-units").build(),
              FunctionalUnitDto.newBuilder().setName(name).setKeyVariableName(name).build()));
        }
      }
    }

    @Override
    protected String getKey() {
      return "units";
    }

  }

  private class ReportsSeeder extends ArraySeeder {

    @Override
    protected void onSeed(JSONArray seed) throws JSONException, IOException {
      for(int i = 0; i < seed.length(); i++) {
        JSONObject json = seed.getJSONObject(i);
        String name = json.getString("name");
        HttpResponse r = opalClient.get(opalClient.newUri().segment("report-template", name).build());
        r.getEntity().consumeContent();
        if(r.getStatusLine().getStatusCode() == SC_NOT_FOUND) {
          System.out.println("Creating report " + name);
          ignore(opalClient.post(opalClient.newUri().segment("report-templates").build(),
              ReportTemplateDto.newBuilder().setName(name).setFormat(json.optString("format", "PDF"))
                  .setDesign(json.optString("design", "/report designs/" + name + ".rptdesign"))
                  .addAllEmailNotification(json.optJSONArray("emails") == null
                      ? Collections.<String>emptyList()
                      : new JSONArrayIterable<String>(json.optJSONArray("emails"))).build()));
        }
      }
    }

    @Override
    protected String getKey() {
      return "reports";
    }

  }

  private class FileSystemSeeder implements Seeder {

    @Override
    public void seed(JSONObject seed) throws JSONException, IOException {
      String fs = seed.optString("fs");
      if(fs != null) {
        File fsRoot;
        fsRoot = fs.startsWith("/") ? new File(fs) : new File(seedFile.getParentFile(), fs);

        if(fsRoot.exists() && fsRoot.isDirectory() && fsRoot.canRead()) {
          System.out.println("Seeding file system from " + fsRoot.getAbsolutePath());
          seedDir(opalClient.newUri().segment("files", "meta"), opalClient.newUri().segment("files"), fsRoot);
          System.out.println("Done.");
        } else {
          System.err.println("Invalid directory for filesystem seed: " + fsRoot.getAbsolutePath());
        }
      }
    }

    private FileDto getChild(FileDto dto, String name) {
      for(FileDto child : dto.getChildrenList()) {
        if(child.getName().equals(name)) {
          return child;
        }
      }
      return null;
    }

    private void seedDir(UriBuilder folderMeta, UriBuilder folder, File dir) throws IOException {
      FileDto dto = opalClient.getResource(FileDto.class, folderMeta.newBuilder().build(), FileDto.newBuilder());
      for(File f : dir.listFiles()) {
        FileDto child = getChild(dto, f.getName());
        if(child != null && child.getLastModifiedTime() >= f.lastModified()) {
          continue;
        }
        if(f.isDirectory()) {
          if(child == null) {
            System.out.println("Creating " + dir.getName() + "/" + f.getName());
            ignore(opalClient.post(folder.build(), f.getName()));
          }
          seedDir(folderMeta.newBuilder().segment(f.getName()), folder.newBuilder().segment(f.getName()), f);
        } else {
          System.out.println("Uploading " + f.getName());
          ignore(opalClient.post(folder.build(), f));
        }
      }
    }

  }

  private class ViewsSeeder extends ArraySeeder {

    @Override
    protected String getKey() {
      return "views";
    }

    @Override
    protected void onSeed(JSONArray seed) throws JSONException, IOException {
      for(int i = 0; i < seed.length(); i++) {
        JSONObject json = seed.getJSONObject(i);
        String name = json.getString("name");
        String destination = json.getString("destination");
        Iterable<String> from = new JSONArrayIterable<String>(json.getJSONArray("from"));
        String file = json.optString("file");

        HttpResponse r = opalClient.get(opalClient.newUri().segment("datasource", destination, "view", name).build());
        r.getEntity().consumeContent();
        if(r.getStatusLine().getStatusCode() == SC_NOT_FOUND) {
          System.out.println(String.format("Creating view %s from file %s into %s", name, file, destination));
          ViewDto dto = ViewDto.newBuilder().setName(name).addAllFrom(from)
              .setExtension(FileViewDto.view, FileViewDto.newBuilder().setFilename(file).build()).build();
          r = opalClient.post(opalClient.newUri().segment("datasource", destination, "views").build(), dto);
          if(r.getStatusLine().getStatusCode() != SC_CREATED) {
            throw new IOException("Could not create view: " + r.getStatusLine().getReasonPhrase());
          }
          ignore(r);
        }
      }

    }
  }

  private class OpalXmlImportSeeder extends ArraySeeder {

    @Override
    protected String getKey() {
      return "xmlImports";
    }

    @Override
    protected void onSeed(JSONArray seed) throws JSONException, IOException {
      for(int i = 0; i < seed.length(); i++) {
        JSONObject json = seed.getJSONObject(i);
        String data = json.getString("data");
        String destination = json.getString("destination");
        String unit = json.optString("unit", null);

        System.out.println("Importing XML file " + data + " into " + destination);
        importXml(data, destination, unit);
      }

    }

    private void importXml(String data, String destination, String unit) throws IOException {

      DatasourceFactoryDto.Builder dsFactoryDtoBuilder = DatasourceFactoryDto.newBuilder();
      dsFactoryDtoBuilder.setName("xml");
      if(unit != null) {
        dsFactoryDtoBuilder.setUnitConfig(Magma.DatasourceUnitConfigDto.newBuilder().setUnit(unit));
      }
      dsFactoryDtoBuilder
          .setExtension(FsDatasourceFactoryDto.params, FsDatasourceFactoryDto.newBuilder().setFile(data).build());

      DatasourceDto csv = createTransientDatasource(dsFactoryDtoBuilder.build());
      ImportCommandOptionsDto.Builder importCommand = ImportCommandOptionsDto.newBuilder().setDestination(destination)
          .setSource(csv.getName());
      if(unit != null) {
        importCommand.setUnit(unit);
      }
      ignore(opalClient.post(opalClient.newUri().segment("shell", "import").build(), importCommand.build()));

    }
  }

  private class CsvImportSeeder extends ArraySeeder {

    @Override
    protected String getKey() {
      return "csvImports";
    }

    @Override
    protected void onSeed(JSONArray seed) throws JSONException, IOException {
      for(int i = 0; i < seed.length(); i++) {
        JSONObject json = seed.getJSONObject(i);
        String data = json.getString("data");
        String dictionary = json.getString("dictionary");
        String destination = json.getString("destination");
        String unit = json.optString("unit", null);

        System.out.println("Importing CSV file " + data + " into " + destination);
        importCsv(data, dictionary, destination, unit);

      }
    }

    private void createTables(DatasourceDto source, String destination) throws IOException {
      UriBuilder sourceUri = opalClient.newUri().segment("datasource", source.getName());
      for(String table : source.getTableList()) {
        HttpResponse r = opalClient.get(opalClient.newUri().segment("datasource", destination, "table", table).build());
        r.getEntity().consumeContent();
        if(r.getStatusLine().getStatusCode() == SC_NOT_FOUND) {
          TableDto t = opalClient.getResource(TableDto.class, sourceUri.newBuilder().segment("table", table).build(),
              TableDto.newBuilder());
          List<VariableDto> v = opalClient
              .getResources(VariableDto.class, sourceUri.newBuilder().segment("table", table, "variables").build(),
                  VariableDto.newBuilder());
          ignore(opalClient.post(opalClient.newUri().segment("datasource", destination, "tables").build(), t));
          ignore(opalClient
              .post(opalClient.newUri().segment("datasource", destination, "table", table, "variables").build(), v));
        }
      }
    }

    private void importCsv(String data, String dictionary, String destination, String unit) throws IOException {
      DatasourceDto variables = createTransientDatasource(DatasourceFactoryDto.newBuilder().setName("none")
          .setExtension(ExcelDatasourceFactoryDto.params,
              ExcelDatasourceFactoryDto.newBuilder().setFile(dictionary).setReadOnly(true).build()).build());

      createTables(variables, destination);

      String refTable = variables.getTable(0);

      DatasourceDto csv = createTransientDatasource(DatasourceFactoryDto.newBuilder().setName("csv")
          .setExtension(CsvDatasourceFactoryDto.params,
              CsvDatasourceFactoryDto.newBuilder().setSeparator(",").setQuote("\"").setCharacterSet("ISO-8859-1")
                  .addTables(CsvDatasourceTableBundleDto.newBuilder().setName(refTable).setData(data)
                      .setRefTable(destination + "." + refTable)).build()).build());
      ImportCommandOptionsDto.Builder importCommand = ImportCommandOptionsDto.newBuilder().setDestination(destination)
          .setSource(csv.getName());
      if(unit != null) {
        importCommand.setUnit(unit);
      }
      ignore(opalClient.post(opalClient.newUri().segment("shell", "import").build(), importCommand.build()));

    }
  }

  private static class JSONArrayIterable<T> implements Iterable<T> {
    private final JSONArray array;

    private JSONArrayIterable(JSONArray array) {
      this.array = array;
    }

    @Override
    public Iterator<T> iterator() {
      return new Iterator<T>() {

        private int nextIndex = 0;

        @Override
        public boolean hasNext() {
          return nextIndex < array.length();
        }

        @SuppressWarnings("unchecked")
        @Override
        public T next() {
          try {
            return (T) array.get(nextIndex++);
          } catch(JSONException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

  }

  private void ignore(HttpResponse r) throws IOException {
    if(r.getEntity() != null) {
      r.getEntity().consumeContent();
    }
    if(r.getStatusLine().getStatusCode() >= SC_BAD_REQUEST) {
      throw new RuntimeException(r.getStatusLine().getReasonPhrase());
    }
  }

  public static void main(String... args) throws IOException, URISyntaxException, JSONException {
    File seedFile = new File(args[0]);

    if(!seedFile.exists() || !seedFile.canRead()) {
      throw new IllegalArgumentException("invalid seed file: " + seedFile.getAbsolutePath());
    }

    Seed s = new Seed(seedFile, args[1], args[2], args[3]);

    s.seed();

  }
}
