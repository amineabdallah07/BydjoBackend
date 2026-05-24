package com.bydjo.config;

import com.bydjo.entity.*;
import com.bydjo.enums.RoleName;
import com.bydjo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== BY DJO Data Initializer ===");

        initRoles();
        initAdminUser();
        initCategories();
        initSizes();
        initColors();

        log.info("=== Data initialization complete ===");
    }

    private void initRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.saveAll(List.of(
                    Role.builder().name(RoleName.ADMIN).build(),
                    Role.builder().name(RoleName.CUSTOMER).build()
            ));
            log.info("Created default roles");
        }
    }

    private void initAdminUser() {
        if (!userRepository.existsByPhone("+21620000000")) {
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("BYDJO")
                    .phone("+21620000000")
                    .email("admin@bydjo.com")
                    .phoneVerified(true)
                    .active(true)
                    .build();
            admin.setRoles(java.util.Set.of(
                    roleRepository.findByName(RoleName.ADMIN).orElseThrow()
            ));
            userRepository.save(admin);
            log.info("Created admin user: +21620000000");
        }
    }

    private void initCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> categories = List.of(
                    Category.builder().name("T-Shirts").slug("t-shirts").description("Premium t-shirts pour homme").active(true).sortOrder(1).build(),
                    Category.builder().name("Jeans").slug("jeans").description("Jeans de qualité supérieure").active(true).sortOrder(2).build(),
                    Category.builder().name("Sneakers").slug("sneakers").description("Sneakers tendance").active(true).sortOrder(3).build(),
                    Category.builder().name("Caps").slug("caps").description("Caps et casquettes stylées").active(true).sortOrder(4).build(),
                    Category.builder().name("Vestes").slug("vestes").description("Vestes et manteaux premium").active(true).sortOrder(5).build(),
                    Category.builder().name("Shorts").slug("shorts").description("Shorts confortables").active(true).sortOrder(6).build(),
                    Category.builder().name("Accessoires").slug("accessoires").description("Accessoires tendance").active(true).sortOrder(7).build()
            );
            categoryRepository.saveAll(categories);
            log.info("Created {} categories", categories.size());
        }
    }

    private void initSizes() {
        if (sizeRepository.count() == 0) {
            List<Size> sizes = List.of(
                    Size.builder().name("XS").build(),
                    Size.builder().name("S").build(),
                    Size.builder().name("M").build(),
                    Size.builder().name("L").build(),
                    Size.builder().name("XL").build(),
                    Size.builder().name("XXL").build(),
                    Size.builder().name("38").build(),
                    Size.builder().name("39").build(),
                    Size.builder().name("40").build(),
                    Size.builder().name("41").build(),
                    Size.builder().name("42").build(),
                    Size.builder().name("43").build(),
                    Size.builder().name("44").build(),
                    Size.builder().name("UNIQUE").build()
            );
            sizeRepository.saveAll(sizes);
            log.info("Created {} sizes", sizes.size());
        }
    }

    private void initColors() {
        if (colorRepository.count() == 0) {
            List<Color> colors = List.of(
                    Color.builder().name("Noir").hexCode("#000000").build(),
                    Color.builder().name("Blanc").hexCode("#FFFFFF").build(),
                    Color.builder().name("Gris").hexCode("#808080").build(),
                    Color.builder().name("Bleu Marine").hexCode("#000080").build(),
                    Color.builder().name("Bleu").hexCode("#0000FF").build(),
                    Color.builder().name("Rouge").hexCode("#FF0000").build(),
                    Color.builder().name("Vert").hexCode("#008000").build(),
                    Color.builder().name("Beige").hexCode("#F5F5DC").build(),
                    Color.builder().name("Rose").hexCode("#FFC0CB").build(),
                    Color.builder().name("Orange").hexCode("#FFA500").build(),
                    Color.builder().name("Jaune").hexCode("#FFD700").build(),
                    Color.builder().name("Marron").hexCode("#8B4513").build()
            );
            colorRepository.saveAll(colors);
            log.info("Created {} colors", colors.size());
        }
    }
}
