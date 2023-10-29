package com.pa1.textdetectionapp.textdetectionapp.service;

import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class FileWriteService {
    public void fileWrite(Map<String, String> mp) {
        try {

            // Open writer
            FileWriter writer = new FileWriter("ImageText.txt");

            // Loop over all data and write to file
            Iterator<Map.Entry<String, String>> iterator = mp.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> pair = iterator.next();
                writer.write(pair.getKey() + ":" + pair.getValue() + "\n");
                iterator.remove();
            }
            writer.close();
            log.info("Write to new file completed! New file created ImageText.txt");
        } catch (IOException e) {
            log.info("Error occurred while writing to the file");
            e.printStackTrace();
        }
    }
}
