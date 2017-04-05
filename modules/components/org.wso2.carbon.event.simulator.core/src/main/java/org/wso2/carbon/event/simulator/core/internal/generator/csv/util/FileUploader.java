/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.simulator.core.internal.generator.csv.util;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.simulator.core.exception.FileAlreadyExistsException;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.exception.ValidationFailedException;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.internal.util.ValidatedInputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class used to upload and delete the CSV file which is uploaded by user.
 */

public class FileUploader {
    private static final Logger log = Logger.getLogger(FileUploader.class);
    /**
     * FileUploader Object which has private static access to create singleton object
     */
    private static final FileUploader fileUploader = new FileUploader(FileStore.getFileStore());
    /**
     * FileStore object which holds details of uploaded file
     */
    private FileStore fileStore;

    private FileUploader(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    /**
     * getFileUploaderInstance() returns Singleton FileUploader object
     *
     * @return fileUploader
     */
    public static FileUploader getFileUploaderInstance() {
        return fileUploader;
    }

    /**
     * Method to upload a CSV file.
     * @param fileName name of file being uploaded
     * @param fileLocation location of the file
     * @throws ValidationFailedException  throw exception if csv file validation failure
     * @throws FileAlreadyExistsException if the file exists in 'tmp/eventSimulator' directory
     * @throws FileOperationsException    if an IOException occurs while copying uploaded stream to
     *                                    'tmp/eventSimulator' directory
     */
    public void uploadFile(String fileName, String fileLocation)
            throws ValidationFailedException, FileAlreadyExistsException, FileOperationsException {
        // Validate file extension
        if (fileName.endsWith(".csv")) {
            /**
             * check whether the file already exists.
             * if so log it exists.
             * else, add the file
             * */
            if (fileStore.checkExists(fileName)) {
                log.error("File '" + fileName + "' already exists in " +
                        (Paths.get(System.getProperty("java.io.tmpdir"), EventSimulatorConstants.DIRECTORY_NAME))

                                .toString());
                throw new FileAlreadyExistsException("File '" + fileName + "' already exists in " +
                        (Paths.get(System.getProperty("java.io.tmpdir"), EventSimulatorConstants.DIRECTORY_NAME))
                                .toString());
            } else {
//                use ValidatedInputStream to check whether the file size is less than the maximum size allowed
                try (ValidatedInputStream inputStream = new ValidatedInputStream(FileUtils.openInputStream(new
                        File(fileLocation)), 200)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Initialize a File reader for CSV file '" + fileName + "'.");
                    }
                    Files.copy(inputStream,
                            Paths.get(System.getProperty("java.io.tmpdir"), EventSimulatorConstants.DIRECTORY_NAME,
                                    fileName));
                    if (log.isDebugEnabled()) {
                        log.debug("Copied content of file '" + fileName + "' to directory " +
                                (Paths.get(System.getProperty("java.io.tmpdir"),
                                        EventSimulatorConstants.DIRECTORY_NAME)).toString());
                    }
                    fileStore.addFile(fileName);
                } catch (IOException e) {
                    log.error("Error occurred while copying the file '" + fileName + "' to location '" +
                            Paths.get(System.getProperty("java.io.tmpdir"), EventSimulatorConstants.DIRECTORY_NAME,
                                    fileName).toString() + "' : ", e);
                    throw new FileOperationsException("Error occurred while copying the file '" + fileName + "' to " +
                            "location '" + Paths.get(System.getProperty("java.io.tmpdir"), EventSimulatorConstants
                            .DIRECTORY_NAME, fileName).toString() + "' : ", e);
                }
            }
        } else {
            log.error("File '" + fileName + " has an invalid content type. Please upload a valid CSV file .");
            throw new ValidationFailedException("File '" + fileName + " has an invalid content type."
                    + " Please upload a valid CSV file .");
        }
        if (log.isDebugEnabled()) {
            log.debug("Successfully uploaded CSV file '" + fileName + "'");
        }
    }

//  public void uploadFile(FileInfo fileInfo, InputStream inputStream)
//            throws ValidationFailedException, FileAlreadyExistsException, FileOperationsException {
//        String fileName = fileInfo.getFileName();
//        // Validate file extension
//        try {
//            if ((fileInfo.getContentType().compareTo("text/csv")) == 0) {
//                /**
//                 * check whether the file already exists.
//                 * if so log it exists.
//                 * else, add the file
//                 * */
//                if (fileStore.checkExists(fileName)) {
//                    log.error("File '" + fileName + "' already exists in " +
//                            (Paths.get(System.getProperty("java.io.tmpdir"), EventSimulatorConstants.DIRECTORY_NAME))
//
//                                    .toString());
//                    throw new FileAlreadyExistsException("File '" + fileName + "' already exists in " +
//                            (Paths.get(System.getProperty("java.io.tmpdir"), EventSimulatorConstants.DIRECTORY_NAME))
//                                    .toString());
//                } else {
//                    Files.copy(inputStream,
//                            Paths.get(System.getProperty("java.io.tmpdir"), EventSimulatorConstants.DIRECTORY_NAME,
//                                    fileName));
//                    if (log.isDebugEnabled()) {
//                        log.debug("Copied content of file '" + fileName + "' to directory " +
//                                (Paths.get(System.getProperty("java.io.tmpdir"),
//                                        EventSimulatorConstants.DIRECTORY_NAME)).toString());
//                    }
//                    fileStore.addFile(fileInfo);
//                }
//            } else {
//                throw new ValidationFailedException("File '" + fileInfo.getFileName() + " has an invalid
// content type."
//                        + " Please upload a valid CSV file .");
//            }
//        } catch (IOException e) {
//            log.error("Error occurred while copying the file '" + fileName + "' to location '" +
//                    Paths.get(System.getProperty("java.io.tmpdir"), EventSimulatorConstants.DIRECTORY_NAME, fileName)
//                            .toString() + "' : ", e);
//            throw new FileOperationsException("Error occurred while copying the file '" + fileName + "' to
// location '" +
//                    Paths.get(System.getProperty("java.io.tmpdir"), EventSimulatorConstants.DIRECTORY_NAME, fileName)
//                            .toString() + "' : ", e);
//        } finally {
//            IOUtils.closeQuietly(inputStream);
//        }
//        if (log.isDebugEnabled()) {
//            log.debug("Successfully uploaded CSV file '" + fileName + "'");
//        }
//    }
//

    /**
     * Method to delete an uploaded file.
     *
     * @param fileName File Name of uploaded CSV file
     * @throws FileOperationsException if an IOException occurs while deleting file
     */
    public boolean deleteFile(String fileName) throws FileOperationsException {
        try {
            if (fileStore.checkExists(fileName)) {
                fileStore.removeFile(fileName);
                Files.deleteIfExists(Paths.get(System.getProperty("java.io.tmpdir"),
                        EventSimulatorConstants.DIRECTORY_NAME, fileName));
                if (log.isDebugEnabled()) {
                    log.debug("Deleted file '" + fileName + "'");
                }
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            log.error("Error occurred while deleting the file '" + fileName + "' : ", e);
            throw new FileOperationsException("Error occurred while deleting the file '" + fileName + "' : ", e);
        }
    }

}
