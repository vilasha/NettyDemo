package com.nettydemo.common.fixed_length;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nettydemo.common.Utils;
import com.nettydemo.common.fixed_length.entities.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MessageGenerator {
    private static final int LINES_PER_MESSAGE = 5;

    public void generateXml(ArrayList<Message> messages, String filePrefix) throws JAXBException {
        generateXml(messages, filePrefix, LINES_PER_MESSAGE);
    }

    public void generateXml(ArrayList<Message> messages, String filePrefix, int linesPerMessage) throws JAXBException {
        ArrayList<MessageStructure> packedMessages = packMessages(messages, linesPerMessage);
        int fileCounter = 0;
        for (MessageStructure message : packedMessages) {
            fileCounter++;
            File file = new File(filePrefix + String.format("%03d", fileCounter) + ".xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(MessageStructure.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(message, file);
        }
    }

    public void generateJson(ArrayList<Message> messages, String filePrefix) throws IOException {
        generateJson(messages, filePrefix, LINES_PER_MESSAGE);
    }

    public void generateJson(ArrayList<Message> messages, String filePrefix, int linesPerMessage) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        ArrayList<MessageStructure> packedMessages = packMessages(messages, linesPerMessage);
        int fileCounter = 0;
        for (MessageStructure message : packedMessages) {
            fileCounter++;
            objectMapper.writeValue(
                    new FileOutputStream(filePrefix + String.format("%03d", fileCounter) + ".json"),
                    message);
        }
    }

    private ArrayList<MessageStructure> packMessages(ArrayList<Message> messages, int linesPerMessage) {
        ArrayList<MessageStructure> result = new ArrayList<>();
        for (int i = 0; i < messages.size(); i += linesPerMessage) {
            MessageStructure message = new MessageStructure();
            message.setId(i);
            message.setMessageNumber(i);
            message.setTotalMessages((int) Math.ceil((double)messages.size() / linesPerMessage));
            message.setFields(new ArrayList<>());
            // messageGuid
            MessageField field = new MessageField();
            field.setName("messageGuid");
            field.setFieldLength(20);
            field.setDataType("string");
            field.setDefaultValue("00000000");
            field.setMandatory(true);
            field.setValues(new ArrayList<>());
            for (int j = i; j < Math.min(i + linesPerMessage, messages.size()); j++)
                field.getValues().add(messages.get(j).getMessageGuid());
            message.getFields().add(field);
            // senderIp
            field = new MessageField();
            field.setName("senderIp");
            field.setFieldLength(15);
            field.setDataType("string");
            field.setDefaultValue("127.0.0.1");
            field.setMandatory(true);
            field.setValues(new ArrayList<>());
            for (int j = i; j < Math.min(i + linesPerMessage, messages.size()); j++)
                field.getValues().add(messages.get(j).getSenderIp());
            message.getFields().add(field);
            // serviceId
            field = new MessageField();
            field.setName("serviceId");
            field.setFieldLength(20);
            field.setDataType("string");
            field.setDefaultValue("echo");
            field.setMandatory(true);
            field.setValues(new ArrayList<>());
            for (int j = i; j < Math.min(i + linesPerMessage, messages.size()); j++) {
                if (messages.get(j) instanceof LoginMessage)
                    field.getValues().add("login");
                else if (messages.get(j) instanceof ErrorMessage)
                    field.getValues().add("error");
                else
                    field.getValues().add("echo");
            }
            message.getFields().add(field);
            // requestTime
            field = new MessageField();
            field.setName("requestTime");
            field.setFieldLength(15);
            field.setDataType("long");
            field.setDefaultValue("0");
            field.setMandatory(true);
            field.setValues(new ArrayList<>());
            for (int j = i; j < Math.min(i + linesPerMessage, messages.size()); j++)
                field.getValues().add(String.valueOf(messages.get(j).getRequestTime()));
            message.getFields().add(field);
            // login
            field = new MessageField();
            field.setName("login");
            field.setFieldLength(20);
            field.setDataType("string");
            field.setDefaultValue("");
            field.setMandatory(false);
            field.setValues(new ArrayList<>());
            for (int j = i; j < Math.min(i + linesPerMessage, messages.size()); j++) {
                Message current = messages.get(j);
                field.getValues().add( (current instanceof LoginMessage) ? ((LoginMessage) current).getLogin() : "" );
            }
            message.getFields().add(field);
            // password
            field = new MessageField();
            field.setName("password");
            field.setFieldLength(20);
            field.setDataType("string");
            field.setDefaultValue("");
            field.setMandatory(false);
            field.setValues(new ArrayList<>());
            for (int j = i; j < Math.min(i + linesPerMessage, messages.size()); j++) {
                Message current = messages.get(j);
                field.getValues().add( (current instanceof LoginMessage) ? ((LoginMessage) current).getPassword() : "" );
            }
            message.getFields().add(field);
            // error-code
            field = new MessageField();
            field.setName("error-code");
            field.setFieldLength(8);
            field.setDataType("string");
            field.setDefaultValue("");
            field.setMandatory(false);
            field.setValues(new ArrayList<>());
            for (int j = i; j < Math.min(i + linesPerMessage, messages.size()); j++) {
                Message current = messages.get(j);
                field.getValues().add( (current instanceof ErrorMessage) ? ((ErrorMessage) current).getErrorCode() : "" );
            }
            message.getFields().add(field);
            // error-code
            field = new MessageField();
            field.setName("error-description");
            field.setFieldLength(200);
            field.setDataType("string");
            field.setDefaultValue("");
            field.setMandatory(false);
            field.setValues(new ArrayList<>());
            for (int j = i; j < Math.min(i + linesPerMessage, messages.size()); j++) {
                Message current = messages.get(j);
                field.getValues().add( (current instanceof ErrorMessage) ? ((ErrorMessage) current).getErrorDescription() : "" );
            }
            message.getFields().add(field);

            result.add(message);
        }
        return result;
    }

    public static void main(String[] args) throws JAXBException, IOException {
        MessageGenerator generator = new MessageGenerator();
        ArrayList<Message> output = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            LoginMessage item = new LoginMessage();
            item.setMessageGuid(Utils.getInstance().getNextMessageId());
            item.setSenderIp(Utils.getInstance().getHost());
            item.setRequestTime(Utils.getInstance().getCurrentDateTime());
            item.setLogin("login" + String.format("%02d", i));
            item.setPassword("qwerty");
            output.add(item);
            ErrorMessage item1 = new ErrorMessage();
            item1.setMessageGuid(Utils.getInstance().getNextMessageId());
            item1.setSenderIp(Utils.getInstance().getHost());
            item1.setRequestTime(Utils.getInstance().getCurrentDateTime());
            item1.setErrorCode("ERR100" + (i + 1));
            item1.setErrorDescription("Error reason text (for error messages only)");
            output.add(item1);
        }
        generator.generateXml(output, "test");
        generator.generateJson(output, "test");
    }
}
