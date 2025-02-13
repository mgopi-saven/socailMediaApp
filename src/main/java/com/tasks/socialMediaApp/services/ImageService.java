package com.tasks.socialMediaApp.services;

import com.tasks.socialMediaApp.Exceptions.AccessException;
import com.tasks.socialMediaApp.Exceptions.InternalServerException;
import com.tasks.socialMediaApp.Exceptions.NotFoundException;
import com.tasks.socialMediaApp.model.Image;
import com.tasks.socialMediaApp.model.Post;
import com.tasks.socialMediaApp.model.User;
import com.tasks.socialMediaApp.repositories.ImageRepository;
import com.tasks.socialMediaApp.requestModel.RequestImage;
import com.tasks.socialMediaApp.responseModel.ResponseImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    ImageRepository imageRepository;
    PostService postService;

    @Autowired
    ImageService(ImageRepository imageRepository,@Lazy PostService postService){
        this.imageRepository = imageRepository;
    }

    public ResponseImage handleAddImageToExistingPost(User user,int postId,Image image){

        Optional<Post> optionalPost = postService.findPost(postId);
        if (optionalPost.isEmpty()) throw new NotFoundException("post not found");
        Post post = optionalPost.get();
        if(postService.isPostOwnedByUser(postId, user.getId())) throw new AccessException("you can't access the post, so you" +
                "can't add image to it");

        Image savedImage = addImageToAPost(image, post);
        if(savedImage == null) throw new InternalServerException("internal server problem");

        return buildResponseImage(savedImage);
    }

    public void handleDeleteImageOfAPost(User user, int postId, int imageId){

        Optional<Post> optionalPost = postService.findPost(postId);
        if (optionalPost.isEmpty()) throw new NotFoundException("post not found");
        Post post = optionalPost.get();

        if(postService.isPostOwnedByUser(postId, user.getId())) throw new AccessException("you can't access these post," +
                "so you can't delete a image of it");
        Image image = findImageByPost(imageId,post);
        if(image == null) throw new NotFoundException("Image not found");

        if(!deleteImage(image)) throw new InternalServerException("internal server problem");
    }

    public List<ResponseImage> handleGetImages(int postId){

        Optional<Post> optionalPost = postService.findPost(postId);
        if (optionalPost.isEmpty()) throw new NotFoundException("post not found");
        Post post = optionalPost.get();

        List<Image> allImages = getImagesOfAPost(post);
        return buildResponseImageList(allImages);
    }
    public Image addImageToAPost(Image image, Post post){

        Image savedEntity = null;
        try{
            image.setPost(post);
            savedEntity = imageRepository.save(image);
        }catch (Exception e){
           logger.error(e.getMessage());
            return null;
        }

        return savedEntity;

    }

    public Image findImage(int imageId){

        Optional<Image> optionalImage = imageRepository.findById(imageId);
        return optionalImage.orElse(null);
    }

    public Image findImageByPost(int imageId, Post post){

        return imageRepository.findByPostAndId(post,imageId);
    }

    public boolean deleteImage(Image image){

        try {
            imageRepository.delete(image);
        }catch (Exception e){
           logger.error(e.getMessage());
            return false;
        }

        return true;
    }

    public List<Image> getImagesOfAPost(Post post){

        List<Image> allImages = null;
        try {
            allImages = imageRepository.findAllByPost(post);
        }catch (Exception e){
           logger.error(e.getMessage());
            return null;
        }

        return allImages;
    }

    public void saveImages(List<RequestImage> requestImages, Post post){

        requestImages.stream()
                .map(requestImage -> {
                    Image imageToSave = new Image();
                    imageToSave.setPost(post);
                    imageToSave.setUrl(requestImage.getUrl());
                    return imageToSave;
                })
                .forEach(imageRepository::save);

    }

    public ResponseImage buildResponseImage(Image savedImage){

        ResponseImage responseImage = new ResponseImage();
        responseImage.setId(savedImage.getId());
        responseImage.setUrl(savedImage.getUrl());

        return responseImage;
    }

    public List<ResponseImage> buildResponseImageList(List<Image> allImages){
        List<ResponseImage> allResponseImages = new ArrayList<>();

        allImages.stream()
                .map(this::buildResponseImage)
                .forEach(allResponseImages::add);

        return allResponseImages;
    }

    public void deleteImagesOfAPost(Post post){

        imageRepository.deleteByPost(post);
    }
}
