package com.itmo.mse.soft.service;

import com.itmo.mse.soft.entity.Body;
import com.itmo.mse.soft.entity.Reader;
import com.itmo.mse.soft.repository.ReaderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class ReaderService extends EntityService<Reader> {

    @Autowired
    private ReaderRepository readerRepository;
    @Autowired
    private BodyService bodyService;
    @Autowired
    private FridgeService fridgeService;

    @Override
    protected CrudRepository<Reader, UUID> getEntityRepository() {
        return readerRepository;
    }

    public Body scanBarcode(UUID readerId, String barcode) {
        var body = bodyService.getBodyByBarcode(barcode).orElseThrow();
        var reader = readerRepository.findById(readerId).orElseThrow();

        switch (reader.getLocation()) {
            case AT_FRIDGE_ENTRANCE:
                return fridgeService.enterFridge(body);
            case AT_FRIDGE_EXIT:
                return fridgeService.closeFridge(body);
        }
        return null;
    }

}
