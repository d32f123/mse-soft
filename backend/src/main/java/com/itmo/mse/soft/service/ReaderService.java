package com.itmo.mse.soft.service;

import com.itmo.mse.soft.entity.Body;
import com.itmo.mse.soft.entity.Reader;
import com.itmo.mse.soft.entity.ReaderLocation;
import com.itmo.mse.soft.repository.ReaderRepository;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

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
    protected JpaRepository<Reader, UUID> getEntityRepository() {
        return readerRepository;
    }

    public Body scanBarcode(ReaderLocation readerLocation, String barcode) {
        Body body = bodyService.getBodyByBarcode(barcode).orElseThrow(() -> new RuntimeException());
        Reader reader = readerRepository.findByLocation(readerLocation).orElseThrow(() -> new RuntimeException());

        switch (reader.getLocation()) {
            case AT_FRIDGE_ENTRANCE:
                return fridgeService.enterFridge(body);
            case AT_FRIDGE_EXIT:
                return fridgeService.closeFridge(body);
        }
        return null;
    }

}
