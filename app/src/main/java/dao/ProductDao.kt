package br.unisanta.approomproduto.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import br.unisanta.approomproduto.model.Product
import androidx.room.Delete
import androidx.room.Update
@Dao
interface ProductDao {
    // suspend: o Room não permite consultas na main thread
    @Query("SELECT * FROM product")
    suspend fun getAll(): List<Product>
    @Insert
    suspend fun insertAll(vararg products: Product)
    @Query("SELECT * FROM product WHERE name LIKE :nome")
    suspend fun buscarPorNome(nome: String): List<Product>
    @Query("DELETE FROM product")
    suspend fun deleteAll()
    @Delete
    suspend fun deleteProduct(product: Product)
    @Update
    suspend fun update(product: Product)
}
