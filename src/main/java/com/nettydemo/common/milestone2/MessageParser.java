package com.nettydemo.common.milestone2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nettydemo.common.milestone2.entities.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Class decodes data from an xml, json, csv or fixed-length txt file
 * into an array list of Message instances. Supports pagination,
 * i.e. data from several files will be combined into one ArrayList
 */
public class MessageParser {

    /**
     * Main method that reads data from files and depending on file's
     * extension calls parsing from methods parseXml, parseJson, parseCsv
     * or parseFixedLength.
     * Then it converts ArrayList of MessageStructure into an ArrayList
     * of Message instances
     * @param fileNames inbound files (xml, json, csv or txt are supported)
     * @return ArrayList of Message instances
     * @throws IOException is thrown if error occurs during json, csv or
     *                     fixed-length parsing
     * @throws JAXBException is thrown if an error occurs during xml parsing
     */
    public ArrayList<Message> parseFiles(String[] fileNames) throws IOException, JAXBException {
        ArrayList<MessageStructure> parsedFiles = new ArrayList<>();
        for (String fileName : fileNames) {
            // Cache in memory for performance
            byte[] mapData = Files.readAllBytes(Paths.get(fileName));
            MessageStructure item;
            String extension = fileName.substring(fileName.length() - 4);
            switch (extension) {
                case ".xml": item = parseXml(mapData);
                             break;
                case "json": item = parseJson(mapData);
                    break;
                case ".csv": item = parseCsv(mapData);
                    break;
                case ".txt": item = parseFixedLength(mapData);
                    break;
                default:
                    throw new UnsupportedOperationException("Only files xml, json, csv and fixed length txt are supported");
            }
            if (item != null)
                parsedFiles.add(item);
        }
        ArrayList<Message> result = new ArrayList<>();
        for (MessageStructure pack : parsedFiles) {
            ArrayList<Message> temp = new ArrayList<>();
            MessageField serviceId = findByName(pack, "serviceId");
            if (serviceId == null)
                continue;
            int totalObjects = serviceId.getValues().size();
            for (int i = 0; i < totalObjects; i++) {
                Message m = null;
                switch (serviceId.getValues().get(i)) {
                    case "login": m = new LoginMessage();
                                  break;
                    case "error": m = new ErrorMessage();
                                  break;
                }
                temp.add(m);
            }
            for (MessageField field : pack.getFields()) {
                switch (field.getName()) {
                    case "messageGuid":
                        for (int i = 0; i < field.getValues().size(); i++)
                            temp.get(i).setMessageGuid(field.getValues().get(i));
                        break;
                    case "senderIp":
                        for (int i = 0; i < field.getValues().size(); i++)
                            temp.get(i).setSenderIp(field.getValues().get(i));
                        break;
                    case "requestTime":
                        for (int i = 0; i < field.getValues().size(); i++)
                            temp.get(i).setRequestTime(Long.parseLong(field.getValues().get(i)));
                        break;
                    case "login":
                        for (int i = 0; i < field.getValues().size(); i++)
                            if (!"".equals(field.getValues().get(i).trim()))
                                ((LoginMessage)temp.get(i)).setLogin(field.getValues().get(i));
                        break;
                    case "password":
                        for (int i = 0; i < field.getValues().size(); i++)
                            if (!"".equals(field.getValues().get(i).trim()))
                                ((LoginMessage)temp.get(i)).setPassword(field.getValues().get(i));
                        break;
                    case "error-code":
                        for (int i = 0; i < field.getValues().size(); i++)
                            if (!"".equals(field.getValues().get(i).trim()))
                                ((ErrorMessage)temp.get(i)).setErrorCode(field.getValues().get(i));
                        break;
                    case "error-description":
                        for (int i = 0; i < field.getValues().size(); i++)
                            if (!"".equals(field.getValues().get(i).trim()))
                                ((ErrorMessage)temp.get(i)).setErrorDescription(field.getValues().get(i));
                        break;
                }
            }
            result.addAll(temp);
        }
        return result;
    }

    /**
     * Maps data from one xml file into one MessageStructure instance
     * @param mapData data from xml file
     * @return MessageStructure instance with certain messages inside
     *         (number of messages is defined in MessageGenerator.LINES_PER_MESSAGE
     *         variable)
     * @throws JAXBException is thrown if an error occurs during xml parsing
     */
    private MessageStructure parseXml(byte[] mapData) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(MessageStructure.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (MessageStructure)jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(mapData));
    }

    /**
     * Maps data from one json file into one MessageStructure instance
     * @param mapData data from json file
     * @return MessageStructure instance with certain messages inside
     *         (number of messages is defined in MessageGenerator.LINES_PER_MESSAGE
     *         variable)
     * @throws IOException is thrown if an error occurs during json parsing
     */
    private MessageStructure parseJson(byte[] mapData) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(mapData, MessageStructure.class);
    }

    /**
     * Maps data from one csv file into one MessageStructure instance
     * @param mapData data from csv file
     * @return MessageStructure instance with certain messages inside
     *         (number of messages is defined in MessageGenerator.LINES_PER_MESSAGE
     *         variable)
     */
    private MessageStructure parseCsv(byte[] mapData) {
        String[] lines = (new String(mapData)).split("\n");
        MessageStructure result = new MessageStructure();
        String[] items = lines[0].split(",",-1);
        result.setId(Integer.parseInt(items[0]));
        result.setMessageNumber(Integer.parseInt(items[1]));
        result.setTotalMessages(Integer.parseInt(items[2]));
        ArrayList<MessageField> fields = new ArrayList<>();
        result.setFields(fields);
        for (int i = 1; i < lines.length; i++) {
            items = lines[i].split(",", -1);
            switch(items[0]) {
                case "name":
                    for (int j = 1; j < items.length; j++) {
                        MessageField f = new MessageField();
                        f.setName(items[j]);
                        f.setValues(new ArrayList<>());
                        fields.add(f);
                    }
                    break;
                case "data-type":
                    for (int j = 1; j < items.length; j++)
                        fields.get(j-1).setDataType(items[j]);
                    break;
                case "default-value":
                    for (int j = 1; j < items.length; j++)
                        fields.get(j-1).setDefaultValue(items[j]);
                    break;
                case "is-mandatory":
                    for (int j = 1; j < items.length; j++)
                        fields.get(j-1).setMandatory(Boolean.parseBoolean(items[j]));
                    break;
                case "value":
                    for (int j = 1; j < items.length; j++)
                        fields.get(j-1).getValues().add(items[j]);
                    break;
            }
        }
        return result;
    }

    /**
     * Maps data from one fixed length txt file into one MessageStructure instance
     * @param mapData data from txt file
     * @return MessageStructure instance with certain messages inside
     *         (number of messages is defined in MessageGenerator.LINES_PER_MESSAGE
     *         variable)
     */
    private MessageStructure parseFixedLength(byte[] mapData) {
        String[] lines = (new String(mapData)).split("\n");
        MessageStructure result = new MessageStructure();
        String[] items = lines[0].split("(?<=\\G.{15})");
        result.setId(Integer.parseInt(items[0].trim()));
        result.setMessageNumber(Integer.parseInt(items[1].trim()));
        result.setTotalMessages(Integer.parseInt(items[2].trim()));
        ArrayList<MessageField> fields = new ArrayList<>();
        result.setFields(fields);
        for (int i = 1; i < lines.length; i++) {
            String s = lines[i].trim();
            if (s.startsWith("name")) {
                s = "name";
                items = lines[i].substring(20).split("(?<=\\G.{20})");
            }
            else if (s.startsWith("data-type")) {
                s = "data-type";
                items = lines[i].substring(20).split("(?<=\\G.{15})");
            }
            else if (s.startsWith("default-value")) {
                s = "default-value";
                items = lines[i].substring(20).split("(?<=\\G.{200})");
            }
            else if (s.startsWith("is-mandatory")) {
                s = "is-mandatory";
                items = lines[i].substring(20).split("(?<=\\G.{5})");
            }
            else if (s.startsWith("value")) {
                s = "value";
                lines[i] = lines[i].substring(20);
                items = new String[fields.size()];
                for (int j = 0; j < items.length; j++) {
                    int fieldLength = 0;
                    switch (fields.get(j).getName()) {
                        case "name":
                        case "messageGuid":
                        case "serviceId":
                        case "login":
                        case "password":
                            fieldLength = 20;
                            break;
                        case "senderIp":
                        case "requestTime":
                            fieldLength = 15;
                            break;
                        case "error-code":
                            fieldLength = 8;
                            break;
                        case "error-description":
                            fieldLength = 200;
                            break;
                    }
                    items[j] = lines[i].substring(0, fieldLength);
                    lines[i] = lines[i].substring(fieldLength);
                }
            }
            for (int j = 0; j < items.length; j++)
                items[j] = items[j].trim();
            switch(s) {
                case "name":
                    for (String item : items) {
                        MessageField f = new MessageField();
                        f.setName(item);
                        f.setValues(new ArrayList<>());
                        fields.add(f);
                    }
                    break;
                case "data-type":
                    for (int j = 0; j < items.length; j++)
                        fields.get(j).setDataType(items[j]);
                    break;
                case "default-value":
                    for (int j = 0; j < items.length; j++)
                        fields.get(j).setDefaultValue(items[j]);
                    break;
                case "is-mandatory":
                    for (int j = 0; j < items.length; j++)
                        fields.get(j).setMandatory(Boolean.parseBoolean(items[j]));
                    break;
                case "value":
                    for (int j = 0; j < items.length; j++)
                        fields.get(j).getValues().add(items[j]);
                    break;
            }
        }
        return result;
    }

    /**
     * Finds a field with a certain name from MessageStructure
     * @param message MessageStructure instance
     * @param name what name to search
     * @return this field with all its parameters (data-type, default
     *         value, is-mandatory, etc)
     */
    private MessageField findByName(MessageStructure message, String name) {
        for (MessageField field : message.getFields())
            if (name.equals(field.getName()))
                return field;
        return null;
    }

    /**
     * Method for testing and demo of class's functionality
     * @param args ignored
     * @throws IOException is thrown if error occurs during json, csv or fixed
     *                     length parsing
     * @throws JAXBException is thrown if error occurs during xml parsing
     */
    public static void main(String[] args) throws IOException, JAXBException {
        MessageParser parser = new MessageParser();
        System.out.println("-------------------------------------------");
        System.out.println("----------------XML parsing----------------");
        System.out.println("-------------------------------------------");
        String[] files = {"test001.xml", "test002.xml", "test003.xml", "test004.xml"};
        ArrayList<Message> res1 = parser.parseFiles(files);
        for (Message m : res1)
            System.out.println(m.toString());
        System.out.println("-------------------------------------------");
        System.out.println("----------------JSON parsing---------------");
        System.out.println("-------------------------------------------");
        String[] files1 = {"test001.json", "test002.json", "test003.json", "test004.json"};
        res1 = parser.parseFiles(files1);
        for (Message m : res1)
            System.out.println(m.toString());
        System.out.println("-------------------------------------------");
        System.out.println("----------------CSV parsing----------------");
        System.out.println("-------------------------------------------");
        String[] files2 = {"test001.csv", "test002.csv", "test003.csv", "test004.csv"};
        res1 = parser.parseFiles(files2);
        for (Message m : res1)
            System.out.println(m.toString());
        System.out.println("-------------------------------------------");
        System.out.println("----------Fixed length parsing-------------");
        System.out.println("-------------------------------------------");
        String[] files3 = {"test001.txt", "test002.txt", "test003.txt", "test004.txt"};
        res1 = parser.parseFiles(files3);
        for (Message m : res1)
            System.out.println(m.toString());
    }
}
