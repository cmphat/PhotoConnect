package com.photoconnect.controller;

import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.CustomerPost;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PortfolioCategory;
import com.photoconnect.entity.ServicePackage;
import com.photoconnect.entity.User;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.CustomerPostRepository;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.ServicePackageRepository;
import com.photoconnect.service.ChatService;
import com.photoconnect.service.CustomerPostService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customer/posts")
public class CustomerPostController {

    private static final Logger log = LoggerFactory.getLogger(CustomerPostController.class);

    private final CustomerPostService customerPostService;
    private final CustomerPostRepository customerPostRepository;
    private final PhotographerProfileRepository photographerProfileRepository;
    private final BookingRepository bookingRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final ChatService chatService;

    public CustomerPostController(CustomerPostService customerPostService,
                                  CustomerPostRepository customerPostRepository,
                                  PhotographerProfileRepository photographerProfileRepository,
                                  BookingRepository bookingRepository,
                                  ServicePackageRepository servicePackageRepository,
                                  ChatService chatService) {
        this.customerPostService = customerPostService;
        this.customerPostRepository = customerPostRepository;
        this.photographerProfileRepository = photographerProfileRepository;
        this.bookingRepository = bookingRepository;
        this.servicePackageRepository = servicePackageRepository;
        this.chatService = chatService;
    }

    @PostMapping
    public String createPost(@RequestParam("caption") String caption,
                             @RequestParam(value = "category", required = false) String category,
                             @RequestParam(value = "budget", required = false) BigDecimal budget,
                             @RequestParam(value = "location", required = false) String location,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             @RequestParam(value = "imageUrl", required = false) String imageUrl,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Object sessionUid = session.getAttribute("userId");
        Object sessionRole = session.getAttribute("userRole");

        if (sessionUid == null || sessionRole == null || !"CUSTOMER".equalsIgnoreCase(sessionRole.toString())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in with a Customer account to post a shoot request.");
            return "redirect:/login";
        }

        Long customerUserId = ((Number) sessionUid).longValue();

        PortfolioCategory cat = null;
        if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            try {
                cat = PortfolioCategory.fromFormValue(category);
            } catch (Exception ignored) {
            }
        }

        try {
            customerPostService.createPost(customerUserId, caption, cat, budget, location, imageFile, imageUrl);
            redirectAttributes.addFlashAttribute("successMessage", "Shoot request posted successfully! Photographers can now view and reach out to you.");
        } catch (Exception e) {
            log.error("Failed to create customer post: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create post: " + e.getMessage());
        }

        return "redirect:/feed";
    }

    @GetMapping("/{postId}/contact")
    @Transactional
    public String contactClient(@PathVariable("postId") Long postId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Object sessionUid = session.getAttribute("userId");
        Object sessionRole = session.getAttribute("userRole");

        if (sessionUid == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please sign in to contact the client.");
            return "redirect:/login";
        }

        if (sessionRole == null || !"PHOTOGRAPHER".equalsIgnoreCase(sessionRole.toString())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only photographers can contact clients directly on inquiry posts.");
            return "redirect:/feed";
        }

        Long photographerUserId = ((Number) sessionUid).longValue();

        Optional<PhotographerProfile> profileOpt = photographerProfileRepository.findByUserIdWithUser(photographerUserId);
        if (profileOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please complete your photographer profile to contact clients.");
            return "redirect:/become-photographer";
        }

        PhotographerProfile profile = profileOpt.get();

        Optional<CustomerPost> postOpt = customerPostRepository.findById(postId);
        if (postOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "The requested customer post could not be found.");
            return "redirect:/feed";
        }

        CustomerPost post = postOpt.get();
        User customer = post.getCustomer();

        if (customer == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer account associated with this post was not found.");
            return "redirect:/feed";
        }

        if (customer.getId().equals(photographerUserId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot initiate an inquiry with yourself.");
            return "redirect:/feed";
        }

        // Check if an existing booking already exists for this post
        List<Booking> existingBookings = bookingRepository.findByCustomerAndProfileAndPostId(customer.getId(), profile.getId(), postId);
        Booking booking;
        if (!existingBookings.isEmpty()) {
            booking = existingBookings.get(0);
        } else {
            // Create a new inquiry booking
            BigDecimal agreedPrice = (post.getBudget() != null && post.getBudget().compareTo(BigDecimal.ZERO) > 0)
                    ? post.getBudget()
                    : (profile.getPriceFrom() != null ? profile.getPriceFrom() : new BigDecimal("1500000"));

            String locationStr = (post.getLocation() != null && !post.getLocation().isBlank())
                    ? post.getLocation()
                    : (customer.getLocation() != null && !customer.getLocation().isBlank() ? customer.getLocation() : "Ho Chi Minh City");

            String notesStr = "Inquiry for Customer Post #" + post.getId() + ": "
                    + (post.getCaption() != null ? (post.getCaption().length() > 300 ? post.getCaption().substring(0, 297) + "..." : post.getCaption()) : "Photoshoot Inquiry");

            booking = new Booking(
                    customer,
                    profile,
                    LocalDate.now().plusDays(7),
                    LocalTime.of(10, 0),
                    locationStr,
                    notesStr,
                    agreedPrice
            );
            booking.setBasePrice(agreedPrice);
            booking.setStatus(BookingStatus.PENDING);

            // Match active service package if available
            List<ServicePackage> packages = servicePackageRepository.findByPhotographerProfileIdAndActiveTrueOrderByPriceAsc(profile.getId());
            if (!packages.isEmpty()) {
                ServicePackage selectedPkg = packages.get(0);
                if (post.getCategory() != null) {
                    for (ServicePackage pkg : packages) {
                        if (pkg.getCategory() == post.getCategory()) {
                            selectedPkg = pkg;
                            break;
                        }
                    }
                }
                booking.setServicePackage(selectedPkg);
                booking.setPackageName(selectedPkg.getName());
                booking.setDurationMinutes(selectedPkg.getDurationMinutes());
                booking.setEditedPhotoCount(selectedPkg.getEditedPhotoCount());
                booking.setDeliveryDays(selectedPkg.getDeliveryDays());
            }

            booking = bookingRepository.save(booking);

            // Send introductory greeting message
            try {
                String snippet = post.getCaption() != null
                        ? (post.getCaption().length() > 70 ? post.getCaption().substring(0, 67) + "..." : post.getCaption())
                        : "photoshoot request";
                String introMsg = "Hello " + (customer.getFullName() != null ? customer.getFullName() : "there")
                        + "! I saw your inquiry: \"" + snippet
                        + "\". I am available and would love to work with you! Let's discuss the session details.";
                chatService.sendMessage(booking.getId(), photographerUserId, introMsg);
            } catch (Exception e) {
                log.warn("Failed to send initial chat message for booking #{}: {}", booking.getId(), e.getMessage());
            }
        }

        return "redirect:/bookings/" + booking.getId() + "/chat";
    }
}
