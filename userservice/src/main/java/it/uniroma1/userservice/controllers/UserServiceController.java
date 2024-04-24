/**
 * MIT No Attribution
 *
 * Copyright 2024 Giuseppe Valente, Antonio Cipriani, Natalia Mucha, Md Anower Hossain
 *
 *Permission is hereby granted, free of charge, to any person obtaining a copy of this
 *software and associated documentation files (the "Software"), to deal in the Software
 *without restriction, including without limitation the rights to use, copy, modify,
 *merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 *permit persons to whom the Software is furnished to do so.
 *
 *THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 *PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package it.uniroma1.userservice.controllers;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.uniroma1.userservice.entities.ACK;
import it.uniroma1.userservice.entities.OperationType;
import it.uniroma1.userservice.entities.User;
import it.uniroma1.userservice.messaging.MessagePayload;
import it.uniroma1.userservice.messaging.MessageProducer;

@RestController
@Validated
public class UserServiceController {

    Logger logger = LoggerFactory.getLogger(UserServiceController.class);

    @Autowired
    private MessageProducer messageProducer;

    @GetMapping("/api/user/hello")
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<String> protectedResourceExample() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body("Hello!");

        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/api/user/insert")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<String> insertUser(@Valid @RequestBody UserInsertModel userModel) {
        try {
            logger.info("inserUser()");
            User u = userModel.toUser();
            MessagePayload mp = new MessagePayload(OperationType.INSERT, u, null);
            String response = messageProducer.sendMessage(mp);
            if (response != null) {
                //ACK RECEIVED
                ObjectMapper om = new ObjectMapper();
                ACK<Long> ack = om.readValue(response, new TypeReference<ACK<Long>>() {});
                if (ack != null) {
                    if (ack.isSuccess()) {
                        if (ack.getPayload() != null) {
                            om = new ObjectMapper();
                            String bodyResponse = om.writeValueAsString(ack);
                            return ResponseEntity.status(HttpStatus.OK).body(bodyResponse);    
                        } else {
                            return ResponseEntity.status(HttpStatus.OK).body("{ 'response' : 'OK'}");    
                        } 
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ack.getMessage());
                    }
                } else {
                    //ERROR
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Can't complete the operation");
                }
            } else {
                //REQUEST NOT PERFORMED
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("At the moment is not possible satisfy the operation request");
            }
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/api/user/edit")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<String> editUser(@Valid @RequestBody UserEditModel userModel) {
        try {
            logger.info("inserUser()");
            User u = userModel.toUser();
            MessagePayload mp = new MessagePayload(OperationType.UPDATE, u, null);
            String response = messageProducer.sendMessage(mp);
            if (response != null) {
                //ACK RECEIVED
                ObjectMapper om = new ObjectMapper();
                ACK<Long> ack = om.readValue(response, new TypeReference<ACK<Long>>() {});
                if (ack != null) {
                    if (ack.isSuccess()) {
                        if (ack.getPayload() != null) {
                            om = new ObjectMapper();
                            String bodyResponse = om.writeValueAsString(ack);
                            return ResponseEntity.status(HttpStatus.OK).body(bodyResponse);    
                        } else {
                            return ResponseEntity.status(HttpStatus.OK).body("{ 'response' : 'OK'}");    
                        } 
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ack.getMessage());
                    }
                } else {
                    //ERROR
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Can't complete the operation");
                }
            } else {
                //REQUEST NOT PERFORMED
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("At the moment is not possible satisfy the operation request");
            }
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/api/user/delete/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable long id) {
        
        try {
            logger.info("deleteUser()");
            User u = new User();
            u.setId(id);
            MessagePayload mp = new MessagePayload(OperationType.DELETE, u, null);
            String response = messageProducer.sendMessage(mp);
            if (response != null) {
                ObjectMapper om = new ObjectMapper();
                ACK<Long> ack = om.readValue(response, new TypeReference<ACK<Long>>() {});
                if (ack != null) {
                    if (ack.isSuccess()) {
                        om = new ObjectMapper();
                        String bodyResponse = om.writeValueAsString(ack);
                        return ResponseEntity.status(HttpStatus.OK).body(bodyResponse);    
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ack.getMessage());
                    }
                } else {
                    //ERROR
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Can't complete the operation");
                }
            
            } else {
                //REQUEST NOT PERFORMED
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("At the moment is not possible satisfy the operation request");
            }

        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }    
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/api/user/view")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<String> viewUsers(@RequestParam(required = false) String searchString) {
        
        try {
            logger.info("viewUsers()");
            MessagePayload mp = new MessagePayload();
            mp.setOperationType(OperationType.SEARCH);
            mp.setSearchString(searchString);
            String response = messageProducer.sendMessage(mp);
            if (response != null) {
                ObjectMapper om = new ObjectMapper();
                ACK<Object> ack = om.readValue(response, new TypeReference<ACK<Object>>() {});
                if (ack != null) {
                    if (ack.isSuccess()) {
                        om = new ObjectMapper();
                        String bodyResponse = om.writeValueAsString(ack.getPayload());
                        return ResponseEntity.status(HttpStatus.OK).body(bodyResponse);    
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ack.getMessage());
                    }
                } else {
                    //ERROR
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Can't complete the operation");
                }
            
            } else {
                //REQUEST NOT PERFORMED
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("At the moment is not possible satisfy the operation request");
            }
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }  
    }
}
