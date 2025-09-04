package uk.gov.hmcts.appregister.applicationentry.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.appregister.applicationentry.dto.IdentityDetailsDto;
import uk.gov.hmcts.appregister.applicationentry.dto.IdentityDetailsWriteDto;
import uk.gov.hmcts.appregister.common.entity.NameAddress;

@Component
public class NameAddressMapper {

    public IdentityDetailsDto toReadDto(NameAddress entity) {
        return new IdentityDetailsDto(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getTitle(),
                entity.getForename_1(),
                entity.getForename_2(),
                entity.getForename_3(),
                entity.getSurname(),
                entity.getAddress_l1(),
                entity.getAddress_l2(),
                entity.getAddress_l3(),
                entity.getAddress_l4(),
                entity.getAddress_l5(),
                entity.getPostcode(),
                entity.getEmail_address(),
                entity.getTelephone_number(),
                entity.getTelephone_number(),
                entity.getDate_of_birth());
    }

    public NameAddress createFromWriteDto(IdentityDetailsWriteDto dto) {
        return NameAddress.builder()
                .code(dto.code())
                .name(dto.name())
                .title(dto.title())
                .forename_1(dto.forename1())
                .forename_2(dto.forename2())
                .forename_3(dto.forename3())
                .surname(dto.surname())
                .address_l1(dto.addressLine1())
                .address_l2(dto.addressLine2())
                .address_l3(dto.addressLine3())
                .address_l4(dto.addressLine4())
                .address_l5(dto.addressLine5())
                .postcode(dto.postcode())
                .email_address(dto.emailAddress())
                .telephone_number(dto.telephoneNumber())
                .mobile_number(dto.mobileNumber())
                .date_of_birth(dto.dateOfBirth())
                .build();
    }

    public void updateFromWriteDto(IdentityDetailsWriteDto dto, NameAddress entity) {
        entity.setCode(dto.code());
        entity.setName(dto.name());
        entity.setTitle(dto.title());
        entity.setForename_1(dto.forename1());
        entity.setForename_2(dto.forename2());
        entity.setForename_3(dto.forename3());
        entity.setSurname(dto.surname());
        entity.setAddress_l1(dto.addressLine1());
        entity.setAddress_l2(dto.addressLine2());
        entity.setAddress_l3(dto.addressLine3());
        entity.setAddress_l4(dto.addressLine4());
        entity.setAddress_l5(dto.addressLine5());
        entity.setPostcode(dto.postcode());
        entity.setEmail_address(dto.emailAddress());
        entity.setTelephone_number(dto.telephoneNumber());
        entity.setMobile_number(dto.mobileNumber());
        entity.setDate_of_birth(dto.dateOfBirth());
    }
}
