package tech.ayot.ticket.backend;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import tech.ayot.ticket.backend.model.user.Role;
import tech.ayot.ticket.backend.model.user.User;
import tech.ayot.ticket.backend.model.user.UserProduct;
import tech.ayot.ticket.backend.repository.user.RoleRepository;
import tech.ayot.ticket.backend.repository.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to initialize application.
 * <p>
 * Roles and admin user are created in initialize method of this class.
 * </p>
 */
@Component
class Initializer implements ApplicationListener<ContextRefreshedEvent> {

    /**
     * The default admin username
     */
    private static final String ADMIN_USERNAME = "admin";

    /**
     * The default admin encoded password
     */
    private static final String ADMIN_PASSWORD = "$2a$10$9tRIOcrkCnQJ30lRkuL2rOODTxhp7D6SDx.pVRWmeu3A.XaVUSmoq";

    /**
     * Array of role titles
     * <p>
     * should be sorted ascending by access level ascending
     * </p>
     */
    private static final String[] ROLE_TITLES = {"GUEST", "USER", "ADMIN"};


    private final PlatformTransactionManager transactionManager;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    public Initializer(
        PlatformTransactionManager transactionManager,
        UserRepository userRepository,
        RoleRepository roleRepository
    ) {
        this.transactionManager = transactionManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }


    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent contextRefreshedEvent) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(null);
        initialize();
        transactionManager.commit(transactionStatus);
    }


    private void initialize() {
        List<Role> roles = createRoles();
        createAdminUser(roles);
    }

    private List<Role> createRoles() {
        List<Role> roles = new ArrayList<>();
        for (String title : ROLE_TITLES) {
            Role role = new Role();
            role.setTitle(title);
            roleRepository.save(role);
            roles.add(role);
        }
        return roles;
    }

    private void createAdminUser(List<Role> roles) {
        User adminUser = userRepository.findUserByUsername(ADMIN_USERNAME);

        // Create admin user if it does not exist
        if (adminUser == null) {
            adminUser = new User();

            // Set admin username and password
            adminUser.setUsername(ADMIN_USERNAME);
            adminUser.setPassword(ADMIN_PASSWORD);
        }

        // Check if admin user is not admin for all products
        boolean isAdminForAllProducts = adminUser.getUserProducts().stream()
            .anyMatch(userProduct -> userProduct.getProduct() == null);
        if (!isAdminForAllProducts) {
            // Set admin user as admin for all products
            Role adminRole = roles.get(roles.size() - 1);
            List<UserProduct> adminUserProducts = new ArrayList<>();
            UserProduct adminUserProduct = new UserProduct();
            adminUserProduct.setUser(adminUser);
            adminUserProduct.setProduct(null);
            adminUserProduct.setRole(adminRole);
            adminUserProducts.add(adminUserProduct);
            adminUser.setUserProducts(adminUserProducts);

            userRepository.save(adminUser);
        }
    }
}