package pro.antonshu.client.utils;

import lombok.Data;
import lombok.NoArgsConstructor;
import pro.antonshu.client.entities.dto.DocumentDto;

import java.util.List;

@NoArgsConstructor
@Data
public class Packet {
    private List<DocumentDto> documentDtos;

    public Packet(List<DocumentDto> documentDtos) {
        this.documentDtos = documentDtos;
    }
}
