package sv.edu.udb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sv.edu.udb.dto.CategoriaRequestDTO;
import sv.edu.udb.dto.CategoriaResponseDTO;
import sv.edu.udb.exception.ResourceNotFoundException;
import sv.edu.udb.model.Categoria;
import sv.edu.udb.repository.CategoriaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaResponseDTO crearCategoria(CategoriaRequestDTO dto) {
        Categoria categoria = Categoria.builder()
                .nombre(dto.getNombre())
                .tipo(dto.getTipo())
                .build();
        return mapToDTO(categoriaRepository.save(categoria));
    }

    public List<CategoriaResponseDTO> listarCategorias() {
        return categoriaRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    public CategoriaResponseDTO obtenerCategoriaPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoría no encontrada con id: " + id));
        return mapToDTO(categoria);
    }

    private CategoriaResponseDTO mapToDTO(Categoria c) {
        return CategoriaResponseDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .tipo(c.getTipo())
                .build();
    }
}
