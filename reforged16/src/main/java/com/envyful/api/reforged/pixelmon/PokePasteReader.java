package com.envyful.api.reforged.pixelmon;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.export.PokemonConverterFactory;
import com.pixelmonmod.pixelmon.api.pokemon.export.exception.PokemonImportException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PokePasteReader {

    private final BufferedReader reader;

    private PokePasteReader(BufferedReader reader) {
        this.reader = reader;
    }

    public List<Pokemon> build() {
        List<String> lines = Lists.newArrayList();
        String currentLine;

        while((currentLine = this.readLine(this.reader)) != null) {
            lines.add(currentLine);
        }

        this.closeReader();

        try {
            return PokemonConverterFactory.importText(lines);
        } catch (PokemonImportException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    private void closeReader() {
        try {
            this.reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readLine(BufferedReader reader) {
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static PokePasteReader from(String paste) {
        URL url = getPokePasteURL(paste);

        if(url == null) {
            return null;
        }

        InputStream inputStream = getConnectionStream(url);

        if(inputStream == null) {
            return null;
        }

        return new PokePasteReader(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)));
    }

    private static URL getPokePasteURL(String paste) {
        if (!paste.toLowerCase(Locale.ROOT).endsWith("/raw")) {
            paste += "/raw";
        }

        try {
            return new URL(paste);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static InputStream getConnectionStream(URL url) {
        try {
            return url.openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static PokePasteReader from(File file) {
        if(file == null) {
            return null;
        }

        InputStream inputStream = getFileStream(file);

        if(inputStream == null) {
            return null;
        }

        return new PokePasteReader(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)));
    }

    private static InputStream getFileStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}