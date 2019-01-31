package com.nettydemo.common.fixed_length;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nettydemo.common.fixed_length.entities.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class MessageParser {
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

    private MessageStructure parseXml(byte[] mapData) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(MessageStructure.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (MessageStructure)jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(mapData));
    }

    private MessageStructure parseJson(byte[] mapData) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(mapData, MessageStructure.class);
    }

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

    private MessageStructure parseFixedLength(byte[] mapData) {
        return null;
    }

    private MessageField findByName(MessageStructure message, String name) {
        for (MessageField field : message.getFields())
            if (name.equals(field.getName()))
                return field;
        return null;
    }

    public static void main(String[] args) throws IOException, JAXBException {
        MessageParser parser = new MessageParser();
        String[] files = {"test001.csv", "test002.csv", "test003.csv", "test004.csv"};
        ArrayList<Message> res1 = parser.parseFiles(files);
        for (Message m : res1)
            System.out.println(m.toString());
    }
}
