package com.hsbc.image;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

    private static String UPLOAD_ROOT = "upload-dir";

    private final ResourceLoader resourceLoader;

    private final ImageRepository imageRepository;

    private final MeterRegistry meterRegistry;

    public ImageService(ResourceLoader resourceLoader, ImageRepository imageRepository, MeterRegistry meterRegistry){
        this.resourceLoader = resourceLoader;
        this.imageRepository = imageRepository;
        this.meterRegistry = meterRegistry;
    }

    @Bean
    CommandLineRunner setUp() throws IOException {
        return args -> {
            FileSystemUtils.deleteRecursively(new File(UPLOAD_ROOT));

            Files.createDirectory(Paths.get(UPLOAD_ROOT));

            FileCopyUtils.copy("Kevin", new FileWriter(UPLOAD_ROOT + "/kevin.jpg"));

            FileCopyUtils.copy("Sherry", new FileWriter(UPLOAD_ROOT+"/sherry.jpg"));

            FileCopyUtils.copy("Gunter", new FileWriter(UPLOAD_ROOT+"/gunter.jpg"));
        };
    }

    public Flux<Image> findAllImages(){
        // The below commented coding implemented without MongoDB reactive
        /*
        try{
            return Flux.fromIterable(
                    Files.newDirectoryStream(Paths.get(UPLOAD_ROOT))
            ).map(path -> new Image(String.valueOf(path.hashCode()), path.getFileName().toString()));
        } catch (IOException ioException){
            return Flux.empty();
        }
        */
        return imageRepository.findAll().log("findAllImages");
    }

    public Mono<Resource> findOneImage(String fileName){
        return Mono.fromSupplier(()->
            resourceLoader.getResource("file:" + UPLOAD_ROOT + "/" + fileName)
        ).log("findOneImage");
    }

    public Mono<Void> createImage(Flux<FilePart> files){
        // The below commented coding implemented without MongoDB reactive
        /*
        return files.flatMap(file -> file.transferTo(
                Paths.get(UPLOAD_ROOT, file.filename()).toFile()
        )).then();
        */
        return files
                .log("createImage-files")
                .flatMap(file -> {
                    Mono<Image> saveDatabaseImage = imageRepository.save(
                   new Image(
                           UUID.randomUUID().toString(),
                           file.filename()))
                            .log("createImage-save");

                   Mono<Void> copyFile = Mono.just(
                           Paths.get(UPLOAD_ROOT, file.filename())
                           .toFile())
                           .log("createImage-picktarget")
                           .map(destFile -> {
                               try{
                                   destFile.createNewFile();
                                   return destFile;
                               } catch (IOException e){
                                   throw new RuntimeException(e);
                               }
                           })
                           .log("createImage-newfile")
                           .flatMap(file::transferTo)
                           .log("createImage-copy");

                   //For Actuator metric show the upload file info
                   Mono<Void> countFile = Mono.fromRunnable( ()-> {
                        meterRegistry
                                .summary("files.uploaded.bytes")
                                .record(Paths.get(UPLOAD_ROOT, file.filename()).toFile().length());
                        }
                   );

                   return Mono.when(saveDatabaseImage, copyFile, countFile)
                           .log("createImage-when");
        })
                .log("createImage-flatMap")
                .then()
                .log("createImage-done");
    }

    public Mono<Void> deleteImage(String fileName){
        // The below commented coding implemented without MongoDB reactive
        /*
        return Mono.fromRunnable(()-> {
                    try {
                        Files.deleteIfExists(Paths.get(UPLOAD_ROOT, fileName));
                    } catch (IOException ioException) {
                        throw new RuntimeException(ioException);
                    }
            }
        );
        */
        Mono<Void> deleteDatabaseImage = imageRepository.findByName(fileName)
                .log("deleteImage-find")
                .flatMap(imageRepository::delete)
                .log("deleteImage-record");

        Mono<Object> deleteFile = Mono.fromRunnable(()-> {
                    try {
                        Files.deleteIfExists(Paths.get(UPLOAD_ROOT, fileName));
                    } catch (IOException ioException) {
                        throw new RuntimeException(ioException);
                    }
                }
        ).log("deleteImage-file");
        return Mono.when(deleteDatabaseImage, deleteFile)
                .log("deleteImage-when")
                .then()
                .log("deleteImage-done");
    }

}
