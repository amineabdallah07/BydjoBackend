package com.bydjo.dtos.product;

import lombok.Data;
import java.util.List;

@Data
public class ImageUrlsRequest {
    private List<ImageUrlDto> images;
}