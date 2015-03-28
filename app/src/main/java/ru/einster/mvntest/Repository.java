package ru.einster.mvntest;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Класс для работы с удаленным репозиторием
 *
 */
public class Repository {

    private String url;



    public Repository(String repositoryUrl) {
        this.url = repositoryUrl;
    }

    /**
     * Вспомогательный метод для получения url на скачивание артифакта
     *
     * @param repository
     * @param artifactGroup
     * @param artifactName
     * @param version
     * @return url для скачивания артифакта
     */
    public static String getArtifactUrl(String repository, String artifactGroup, String artifactName,
                                        String version) {

        return repository + "/"
                + artifactGroup.replace('.', '/') + "/" + artifactName + "/" + version + "/"
                + artifactName + "-" + version + ".apk";
    }

    /**
     *
     * @param group
     * @param artifact
     * @return список версий заданного артифакта
     */
    public List<String> getVersions(String group, String artifact) {
        List<String> versions = new ArrayList<>();
        String metadataPath = url + "/" + group.replace(".", "/") + "/" + artifact + "/maven-metadata.xml";

        InputStream is = null;
        try {
            is = getFileInputStream(metadataPath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(is);
            Element root = dom.getDocumentElement();
            NodeList versionsNodes = root.getElementsByTagName("versions");
            Element versionsElement = (Element) versionsNodes.item(0);
            NodeList versionsList = versionsElement.getElementsByTagName("version");
            for (int i = 0; i < versionsList.getLength(); i++) {
                Node versionNode = versionsList.item(i);
                String version = versionNode.getTextContent();
                versions.add(version);
            }
            return versions;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (is != null) {
                IOUtils.closeQuietly(is);
            }
        }
    }

    private InputStream getFileInputStream(String fileUrl) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(fileUrl);
        HttpResponse response = client.execute(request);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return response.getEntity().getContent();
        } else {
            throw new RuntimeException("Wrong http status " + response.getStatusLine().getStatusCode());
        }
    }
}
