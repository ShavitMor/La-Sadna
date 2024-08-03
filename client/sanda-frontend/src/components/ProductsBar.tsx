import React, { useEffect, useRef, useState } from 'react';
import '../styles/ProductCarousel.css';
import { getTopProducts } from '../API';
import ProductModel from '../models/ProductModel';
import { useNavigate } from 'react-router-dom';



type ProductCarouselProps= {
  products: ProductModel[];
}

const ProductsBar = () => {
    const [carouselRef, setcarouselRef]= useState(0);
    const [products, setProducts]= useState([] as ProductModel[]);
    const [currentProduct, setCurrentProducts]= useState([] as ProductModel[]);
        const [carouselIndex, setCarouselIndex] = useState(0);

    const navigate = useNavigate();
  useEffect(() => {
        const fetchProducts = async () => {
            try {
                const response = await getTopProducts();
                const parsedProducts = JSON.parse(response.dataJson) as ProductModel[];
                setProducts(parsedProducts);
                setCurrentProducts(parsedProducts.slice(carouselIndex, carouselIndex + 5));
            } catch (error) {
                console.error('Error fetching products: ', error);
            }
        };

        fetchProducts();
    }, [carouselIndex]);
    const scrollLeft = () => {
        if(carouselRef>0){
            setcarouselRef(carouselRef-1);
        setCurrentProducts(products.slice(carouselRef-1,carouselRef+4));
        }
    };
  
    const scrollRight = () => {
        if(carouselRef<products.length-5){
            setcarouselRef(carouselRef+1);
            setCurrentProducts(products.slice(carouselRef+1,carouselRef+6));
        }
    };
    const handleClick = (product: ProductModel) => {
        navigate(`/product/${product.productID}`);
    }
    return (
      <div className="product-carousel">
        <button className="arrow left" onClick={scrollLeft}>&lt;</button>
        <button className="arrow right" onClick={scrollRight}>&gt;</button>
        <div className="product-list"  >
          {currentProduct.map((product, index) => (
            <div className="product-item" key={index} onClick={() => handleClick(product)}>
              <h3>{product.productName}</h3>
              <p>${product.productPrice}</p>     
            </div>
          ))}
        </div>
        
      </div>
    );
  };
  
export default ProductsBar;


