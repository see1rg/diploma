package ru.skypro.homework.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skypro.homework.models.Image;
@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {
    Image findImageByAds_Id(Integer id);

}
