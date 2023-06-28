package ru.skypro.homework.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dtos.AdsDto;
import ru.skypro.homework.dtos.AdsDtoFull;
import ru.skypro.homework.mappers.AdsMapper;
import ru.skypro.homework.models.Ads;
import ru.skypro.homework.models.Comment;
import ru.skypro.homework.models.User;
import ru.skypro.homework.repositories.AdsRepository;
import ru.skypro.homework.repositories.CommentRepository;
import ru.skypro.homework.repositories.UserRepository;
import ru.skypro.homework.services.AdsService;
import ru.skypro.homework.services.CommentService;
import ru.skypro.homework.services.ImageService;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import static org.springframework.util.ObjectUtils.isEmpty;


@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class AdsServiceImpl implements AdsService {
    private final AdsRepository adsRepository;
    private final ImageService imageService;
    private final UserRepository userRepository;
    private final AdsMapper adsMapper;
    private final CommentService commentService;
    private final CommentRepository commentRepository;

    @Override
    public Collection<AdsDto> getAllAds(String title) {
        if (!isEmpty(title)) {
            Collection<Ads> ads = adsRepository.findByTitleLike(title);
            log.info("Get ads with title: " + title);
            return adsMapper.adsCollectionToAdsDto(ads);
        }
        Collection<Ads> ads = adsRepository.findAll();
        log.info("Get all ads: " + ads);
        return adsMapper.adsCollectionToAdsDto(ads);
    }

    @Override
    public AdsDto addAd(AdsDto adsDto, MultipartFile image, Authentication authentication) throws IOException {
        Ads newAds = adsMapper.adsDtoToAds(adsDto);
        newAds.setAuthorId(userRepository.findUserByUsername(authentication.getName()));
        adsRepository.save(newAds);//todo
        log.info("Save ads: " + newAds);
        if (image != null) {
            imageService.saveImage(newAds.getAuthorId().getId(), image);
            log.info("Photo has been saved");
        } else {
            throw new IOException("Photo not found");
        }
        return adsMapper.adsToAdsDto(newAds);
    }

    @Override
    public AdsDtoFull getAds(Integer id) {
        Ads ads = adsRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Ads not found"));
        log.info("Get ads: " + ads);
        return adsMapper.adsToAdsDtoFull(ads);
    }

    @Override
    public boolean removeAd(Integer id) {
        log.info("Delete ads: " + id);
        Optional<Ads> adOptional = adsRepository.findById(id);
        if (adOptional.isEmpty()) {
            log.info("Ad not found");
            return false;
        }
        Collection<Comment> commentsToDelete = commentRepository.findCommentsByAds_Id(id);
        log.info("Delete comments: " + commentsToDelete);
        for (Comment comment : commentsToDelete) {
            commentService.deleteComment(id, comment.getId());
        }
        adsRepository.deleteById(id);
        return true;
    }

    @Override
    public AdsDto updateAds(AdsDto adsDto, Integer id) {
        Ads ads = adsRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Ads not found"));
        Ads adsToUpdate = adsMapper.adsDtoToAds(adsDto);
        log.info("Update ads: " + ads);
        return adsMapper.adsToAdsDto(adsRepository.save(adsToUpdate));
    }

    @Override
    public Collection<AdsDto> getMe(String email) {
        log.info("Get ads: " + email);
        User author = userRepository.findUserByUsername(email);
        Collection<Ads> ads = adsRepository.findAllByAuthorId(author);
        Collection<AdsDto> adsDto = adsMapper.adsCollectionToAdsDto(ads);
        log.info("Found ads: " + adsDto);
        return adsDto;
    }

    @Override
    public byte[] updateImage(Integer id, MultipartFile image) throws IOException {
        log.info("Update image: " + id);
        imageService.saveImage(id, image);
        log.info("Photo have been saved");
        return image.getBytes();
    }

}
