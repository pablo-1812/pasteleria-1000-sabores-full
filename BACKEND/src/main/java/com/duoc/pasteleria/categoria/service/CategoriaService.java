package com.duoc.pasteleria.categoria.service;

import com.duoc.pasteleria.categoria.dto.CategoriaRequest;
import com.duoc.pasteleria.categoria.dto.CategoriaResponse;
import com.duoc.pasteleria.categoria.exception.ConflictException;
import com.duoc.pasteleria.categoria.exception.NotFoundException;
import com.duoc.pasteleria.categoria.model.Categoria;
import com.duoc.pasteleria.categoria.repository.CategoriaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository repository;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return repository.findAll(Sort.by("id")).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        String nombre = request.nombre().trim();
        if (repository.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictException("Ya existe una categoría con ese nombre");
        }
        Categoria categoria = Categoria.builder()
                .nombre(nombre)
                .descripcion(request.descripcion())
                .build();
        return toResponse(repository.save(categoria));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscar(id);
        String nombre = request.nombre().trim();
        if (repository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ConflictException("Ya existe otra categoría con ese nombre");
        }
        categoria.setNombre(nombre);
        categoria.setDescripcion(request.descripcion());
        return toResponse(repository.save(categoria));
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscar(id);
        try {
            repository.delete(categoria);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("No se puede eliminar la categoría porque tiene productos asociados");
        }
    }

    private Categoria buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada: " + id));
    }

    private CategoriaResponse toResponse(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion());
    }
}