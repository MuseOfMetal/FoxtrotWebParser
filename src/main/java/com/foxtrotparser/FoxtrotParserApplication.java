package com.foxtrotparser;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@RestController
public class FoxtrotParserApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoxtrotParserApplication.class, args);

    }
    @GetMapping("/parse")
    public ResponseEntity<byte[]> parse(@RequestParam(value = "url", defaultValue = "https://www.foxtrot.com.ua/uk/shop/mobilnye_telefony_smartfon.html") String url) throws IOException {
        System.out.println(url);
        var parser = new FoxtrotCategoryParser(url);
        var byteArr = CreateSheet(parser.parse());
        System.out.println(byteArr.toString());
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=result.xlsx");
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");
        return ResponseEntity.ok()
                .headers(header)
                .contentLength(byteArr.length)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(byteArr);
    }
    private byte[] CreateSheet(List<FoxtrotProductInfo> productInfos) throws IOException {

        Workbook book = new HSSFWorkbook();
        Sheet sheet = book.createSheet("Data");

        Row dataRow = sheet.createRow(0);

        List<String> values = new ArrayList<>();
        values.add("Назва");
        values.add("Посилання");
        values.add("Поточна ціна");
        values.add("Стара ціна");
        values.add("Розмір знижки");
        values.addAll(GetAllKeys(productInfos));

        for(int i = 0; i < values.size(); i++){
            Cell cell = dataRow.createCell(i);
            cell.setCellValue(values.get(i));
        }

        for (int i = 0; i < productInfos.size(); i++){
            Row _row = sheet.createRow(i + 1);
            for (int j = 0; j < values.size(); j++){
                Cell _cell = _row.createCell(j);
                if (j == 0){
                    _cell.setCellValue(productInfos.get(i).name);
                }
                else if (j == 1) {
                    _cell.setCellValue("https://www.foxtrot.com.ua" + productInfos.get(i).url);
                }
                else if (j == 2) {
                    _cell.setCellValue(productInfos.get(i).currentPrice);
                }
                else if (j == 3) {
                    _cell.setCellValue(productInfos.get(i).oldPrice);
                }
                else if (j == 4) {
                    _cell.setCellValue(productInfos.get(i).discount);
                }
                else {
                    if (productInfos.get(i).characteristics.containsKey(values.get(j))){
                        _cell.setCellValue(productInfos.get(i).characteristics.get(values.get(j)));
                    }
                    else{
                        continue;
                    }
                }
            }
        }
        for (int i = 0; i< values.size(); i++){
            sheet.autoSizeColumn(i);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        book.write(bos);
        book.close();
        return bos.toByteArray();
    }
    private List<String> GetAllKeys(List<FoxtrotProductInfo> productInfos){
        List<String> strings = new ArrayList<>();
        for(var i : productInfos){
            var keys = i.characteristics.keySet();
            for (var key : keys){
                if (!strings.contains(key)){
                    strings.add(key);
                }
            }
        }
        return strings;
    }

}



