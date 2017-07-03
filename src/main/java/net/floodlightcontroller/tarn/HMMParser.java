package net.floodlightcontroller.tarn;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by geddingsbarrineau on 6/19/17.
 */
public class HMMParser {

    public static HMM parseIntoHMM(String fileName) throws ParseException {
        try {
            File inputFile = new File(fileName);

            SAXBuilder saxBuilder = new SAXBuilder();

            Document document = saxBuilder.build(inputFile);

            Element classElement = document.getRootElement();

            /* Parse states into a list */
            List<HMM.State> states = new ArrayList<>();
            for (Element e : classElement.getChild("states").getChildren()) {
                int stateId = e.getAttribute("id").getIntValue();
                HMM.State state = new HMM.State(stateId);
                states.add(state);
            }

            /* Parse events into a list */
            List<HMM.Event> events = new ArrayList<>();
            for (Element e : classElement.getChild("events").getChildren()) {
                int id = e.getAttribute("id").getIntValue();
                String name = e.getAttributeValue("name");
                String value = e.getAttributeValue("value");
                HMM.Event event = new HMM.Event(id, name, value);
                events.add(event);
            }

            return new HMM(classElement.getAttributeValue("complexity"),
                    classElement.getAttributeValue("entropy_rate"),
                    states, events);

        } catch (JDOMException | IOException e) {
            e.printStackTrace();
            throw new ParseException("Could not parse XML file into HMM", 0);
        }
    }
}
