package com.almahir.iti.service;

import com.almahir.iti.dto.request.UpdateSheikhRequest;
import com.almahir.iti.dto.response.SheikhResponse;
import com.almahir.iti.exception.ResourceNotFoundException;
import com.almahir.iti.mapper.SheikhMapper;
import com.almahir.iti.model.Sheikh;
import com.almahir.iti.model.User;
import com.almahir.iti.model.enums.SheikhStatus;
import com.almahir.iti.repository.SheikhRepository;
import com.almahir.iti.repository.UserRepository;
import com.almahir.iti.service.impl.SheikhServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SheikhServiceImplTest {

    @Mock
    private SheikhRepository sheikhRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SheikhMapper sheikhMapper;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SheikhServiceImpl sheikhService;

    private UUID sheikhId;
    private User mockUser;
    private Sheikh mockSheikh;
    private SheikhResponse mockResponse;

    @BeforeEach
    void setUp() {
        sheikhId = UUID.randomUUID();

        mockUser = User.builder()
                .id(sheikhId)
                .username("sheikh_ahmed")
                .firstName("Ahmed")
                .lastName("Mahmoud")
                .email("ahmed@example.com")
                .phoneNumber("01012345678")
                .password("encoded_old_password")
                .profilePictureUrl("http://example.com/pic.jpg")
                .build();

        mockSheikh = Sheikh.builder()
                .id(sheikhId)
                .user(mockUser)
                .sheikhStatus(SheikhStatus.AVAILABLE)
                .rate(4.8)
                .build();

        mockResponse = new SheikhResponse(
                sheikhId,
                "sheikh_ahmed",
                "Ahmed",
                "Mahmoud",
                "ahmed@example.com",
                "01012345678",
                "http://example.com/pic.jpg",
                SheikhStatus.AVAILABLE,
                4.8
        );
    }

    @Test
    void getAllSheikhs_ShouldReturnList() {
        when(sheikhRepository.findAllWithUser()).thenReturn(List.of(mockSheikh));
        when(sheikhMapper.toSheikhResponse(mockSheikh)).thenReturn(mockResponse);

        List<SheikhResponse> result = sheikhService.getAllSheikhs();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("sheikh_ahmed", result.get(0).username());
        verify(sheikhRepository, times(1)).findAllWithUser();
    }

    @Test
    void getSheikhByEmail_WhenExists_ShouldReturnSheikh() {
        when(sheikhRepository.findByUserEmailFetchUser("ahmed@example.com")).thenReturn(Optional.of(mockSheikh));
        when(sheikhMapper.toSheikhResponse(mockSheikh)).thenReturn(mockResponse);

        SheikhResponse result = sheikhService.getSheikhByEmail("ahmed@example.com");

        assertNotNull(result);
        assertEquals("ahmed@example.com", result.email());
    }

    @Test
    void getSheikhByEmail_WhenNotFound_ShouldThrowResourceNotFound() {
        when(sheikhRepository.findByUserEmailFetchUser("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sheikhService.getSheikhByEmail("unknown@example.com"));
    }

    @Test
    void getSheikhByUsername_WhenExists_ShouldReturnSheikh() {
        when(sheikhRepository.findByUserUsernameFetchUser("sheikh_ahmed")).thenReturn(Optional.of(mockSheikh));
        when(sheikhMapper.toSheikhResponse(mockSheikh)).thenReturn(mockResponse);

        SheikhResponse result = sheikhService.getSheikhByUsername("sheikh_ahmed");

        assertNotNull(result);
        assertEquals("sheikh_ahmed", result.username());
    }

    @Test
    void getSheikhByUsername_WhenNotFound_ShouldThrowResourceNotFound() {
        when(sheikhRepository.findByUserUsernameFetchUser("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sheikhService.getSheikhByUsername("unknown"));
    }

    @Test
    void getSheikhById_WhenExists_ShouldReturnSheikh() {
        when(sheikhRepository.findByIdFetchUser(sheikhId)).thenReturn(Optional.of(mockSheikh));
        when(sheikhMapper.toSheikhResponse(mockSheikh)).thenReturn(mockResponse);

        SheikhResponse result = sheikhService.getSheikhById(sheikhId);

        assertNotNull(result);
        assertEquals(sheikhId, result.id());
    }

    @Test
    void getSheikhById_WhenNotFound_ShouldThrowResourceNotFound() {
        when(sheikhRepository.findByIdFetchUser(sheikhId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sheikhService.getSheikhById(sheikhId));
    }

    @Test
    void updateSheikh_WithValidData_ShouldUpdateFieldsAndReturnResponse() {
        UpdateSheikhRequest request = new UpdateSheikhRequest(
                "newPassword123",
                "Mohamed Ali",
                null,
                null,
                "01112345678",
                null,
                SheikhStatus.BUSY
        );

        when(sheikhRepository.findByIdFetchUser(sheikhId)).thenReturn(Optional.of(mockSheikh));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encoded_newPassword123");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(sheikhRepository.save(any(Sheikh.class))).thenReturn(mockSheikh);

        SheikhResponse expectedResponse = new SheikhResponse(
                sheikhId,
                "sheikh_ahmed",
                "Mohamed",
                "Ali",
                "ahmed@example.com",
                "01112345678",
                "http://example.com/pic.jpg",
                SheikhStatus.BUSY,
                4.8
        );
        when(sheikhMapper.toSheikhResponse(any(Sheikh.class))).thenReturn(expectedResponse);

        SheikhResponse result = sheikhService.updateSheikh(sheikhId, request, null);

        assertNotNull(result);
        assertEquals(SheikhStatus.BUSY, result.sheikhStatus());
        assertEquals("01112345678", result.phoneNumber());
        verify(passwordEncoder, times(1)).encode("newPassword123");
        verify(userRepository, times(1)).save(mockUser);
        verify(sheikhRepository, times(1)).save(mockSheikh);
    }

    @Test
    void updateSheikh_WithMultipartFile_ShouldUploadImageToCloudinary() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(cloudinaryService.uploadFile(eq(file), anyString())).thenReturn("http://cloudinary.com/new_pic.jpg");

        when(sheikhRepository.findByIdFetchUser(sheikhId)).thenReturn(Optional.of(mockSheikh));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(sheikhRepository.save(any(Sheikh.class))).thenReturn(mockSheikh);

        SheikhResponse expectedResponse = new SheikhResponse(
                sheikhId,
                "sheikh_ahmed",
                "Ahmed",
                "Mahmoud",
                "ahmed@example.com",
                "01012345678",
                "http://cloudinary.com/new_pic.jpg",
                SheikhStatus.AVAILABLE,
                4.8
        );
        when(sheikhMapper.toSheikhResponse(any(Sheikh.class))).thenReturn(expectedResponse);

        SheikhResponse result = sheikhService.updateSheikh(sheikhId, null, file);

        assertNotNull(result);
        assertEquals("http://cloudinary.com/new_pic.jpg", result.profilePictureUrl());
        verify(cloudinaryService, times(1)).uploadFile(eq(file), eq("almahir/profile_pictures"));
    }
}
