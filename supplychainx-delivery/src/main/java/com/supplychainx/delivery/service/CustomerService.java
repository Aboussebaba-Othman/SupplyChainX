package com.supplychainx.delivery.service;

import com.supplychainx.common.exception.BusinessException;
import com.supplychainx.common.exception.DuplicateResourceException;
import com.supplychainx.common.exception.ResourceNotFoundException;
import com.supplychainx.delivery.dto.request.CustomerRequestDTO;
import com.supplychainx.delivery.dto.response.CustomerResponseDTO;
import com.supplychainx.delivery.entity.Customer;
import com.supplychainx.delivery.mapper.CustomerMapper;
import com.supplychainx.delivery.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    // Créer un nouveau client
    @Transactional
    public CustomerResponseDTO create(CustomerRequestDTO requestDTO) {
        log.info("Création d'un nouveau client avec le code: {}", requestDTO.getCode());

        // Vérifier si le code existe déjà
        if (customerRepository.existsByCode(requestDTO.getCode())) {
            throw new DuplicateResourceException("Un client avec le code " + requestDTO.getCode() + " existe déjà");
        }

        // Vérifier si l'email existe déjà
        if (requestDTO.getEmail() != null && customerRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("Un client avec l'email " + requestDTO.getEmail() + " existe déjà");
        }

        Customer customer = customerMapper.toEntity(requestDTO);
        Customer savedCustomer = customerRepository.save(customer);

        log.info("Client créé avec succès - ID: {}, Code: {}", savedCustomer.getId(), savedCustomer.getCode());
        return customerMapper.toResponseDTO(savedCustomer);
    }

    // Mettre à jour un client existant
    @Transactional
    public CustomerResponseDTO update(Long id, CustomerRequestDTO requestDTO) {
        log.info("Mise à jour du client ID: {}", id);

        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));

        // Vérifier si le nouveau code existe déjà (sauf pour le client actuel)
        if (!existingCustomer.getCode().equals(requestDTO.getCode()) &&
                customerRepository.existsByCode(requestDTO.getCode())) {
            throw new DuplicateResourceException("Un client avec le code " + requestDTO.getCode() + " existe déjà");
        }

        // Vérifier si le nouvel email existe déjà (sauf pour le client actuel)
        if (requestDTO.getEmail() != null &&
                !requestDTO.getEmail().equals(existingCustomer.getEmail()) &&
                customerRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("Un client avec l'email " + requestDTO.getEmail() + " existe déjà");
        }

        customerMapper.updateEntityFromDTO(requestDTO, existingCustomer);
        Customer updatedCustomer = customerRepository.save(existingCustomer);

        log.info("Client mis à jour avec succès - ID: {}", id);
        return customerMapper.toResponseDTO(updatedCustomer);
    }

    // Récupérer un client par ID
    public CustomerResponseDTO getById(Long id) {
        log.info("Récupération du client ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));

        return customerMapper.toResponseDTO(customer);
    }

    // Récupérer un client par code
    public CustomerResponseDTO getByCode(String code) {
        log.info("Récupération du client avec le code: {}", code);

        Customer customer = customerRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec le code: " + code));

        return customerMapper.toResponseDTO(customer);
    }

    // Récupérer tous les clients avec pagination
    public Page<CustomerResponseDTO> getAll(Pageable pageable) {
        log.info("Récupération de tous les clients - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Customer> customers = customerRepository.findAll(pageable);
        return customers.map(customerMapper::toResponseDTO);
    }

    // Rechercher des clients par mot-clé
    public Page<CustomerResponseDTO> search(String keyword, Pageable pageable) {
        log.info("Recherche de clients avec le mot-clé: {}", keyword);

        Page<Customer> customers = customerRepository.searchCustomers(keyword, pageable);
        return customers.map(customerMapper::toResponseDTO);
    }

    // Récupérer les clients par ville
    public Page<CustomerResponseDTO> getByCity(String city, Pageable pageable) {
        log.info("Récupération des clients de la ville: {}", city);

        Page<Customer> customers = customerRepository.findByCity(city, pageable);
        return customers.map(customerMapper::toResponseDTO);
    }

    // Récupérer les clients par pays
    public Page<CustomerResponseDTO> getByCountry(String country, Pageable pageable) {
        log.info("Récupération des clients du pays: {}", country);

        Page<Customer> customers = customerRepository.findByCountry(country, pageable);
        return customers.map(customerMapper::toResponseDTO);
    }

    // Supprimer un client
    @Transactional
    public void delete(Long id) {
        log.info("Suppression du client ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));

        // Vérifier si le client a des commandes actives
        if (!customer.getDeliveryOrders().isEmpty()) {
            throw new BusinessException("Impossible de supprimer un client avec des commandes actives");
        }

        customerRepository.delete(customer);
        log.info("Client supprimé avec succès - ID: {}", id);
    }
}
