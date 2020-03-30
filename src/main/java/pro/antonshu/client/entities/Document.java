package pro.antonshu.client.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

@NoArgsConstructor
@Data
public class Document {

    private Long id;

    private String title;

    private String path;

    public Document(String title, String path) {
        this.title = title;
        this.path = path;
    }
}
