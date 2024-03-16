import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document license1Document = docBuilder.parse(new File("./License1.xml"));
            Document license2Document = docBuilder.parse(new File("./License2.xml"));

            license1Document.getDocumentElement().normalize();
            license2Document.getDocumentElement().normalize();

            List<String> validLicenseList = new ArrayList<>();
            List<String> invalidLicenseList = new ArrayList<>();

            processLicenseDocument(license1Document, validLicenseList, invalidLicenseList);
            processLicenseDocument(license2Document, validLicenseList, invalidLicenseList);

            writeToFile("validLicenses.txt", validLicenseList);
            writeToFile("invalidLicenses.txt", invalidLicenseList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processLicenseDocument(Document doc, List<String> validList, List<String> invalidList) {
        NodeList producerList = doc.getElementsByTagName("CSR_Producer");
        for (int i = 0; i < producerList.getLength(); i++) {
            Element producer = (Element) producerList.item(i);
            String nipr = producer.getAttribute("NIPR_Number");
            NodeList licenseList = producer.getElementsByTagName("License");
            for (int j = 0; j < licenseList.getLength(); j++) {
                Element license = (Element) licenseList.item(j);
                if (isValidLicense(license)) {
                    validList.add(formatLicenseInfo(license, nipr));
                } else {
                    invalidList.add(formatLicenseInfo(license, nipr));
                }
            }
        }
    }

    private static boolean isValidLicense(Element license) {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate expirationDate = LocalDate.parse(license.getAttribute("License_Expiration_Date"), formatter);
        return expirationDate.isAfter(currentDate) || expirationDate.isEqual(currentDate);
    }

    private static String formatLicenseInfo(Element license, String nipr) {
        StringBuilder sb = new StringBuilder();
        sb.append(nipr).append(", ");
        sb.append(license.getAttribute("License_Number")).append(", ");
        sb.append(license.getAttribute("State_Code")).append(", ");
        sb.append(license.getAttribute("Resident_Indicator")).append(", ");
        sb.append(license.getAttribute("License_Class")).append(", ");
        sb.append(license.getAttribute("Date_Status_Effective")).append(", ");
        sb.append(license.getAttribute("License_Expiration_Date")).append(", ");
        sb.append(license.getAttribute("License_Status"));
        return sb.toString();
    }

    private static void writeToFile(String filename, List<String> licenseList) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("nipr,License ID,Jurisdiction,Resident,License Class,License Effective Date,License Expiry Date,License Status");
            for (String license : licenseList) {
                writer.newLine();
                writer.write(license);
            }
        }
    }
}
