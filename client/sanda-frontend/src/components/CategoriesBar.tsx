import React, { useRef, useState } from 'react';
import '../styles/ProductCarousel.css';
import luxury from '../images/categories/01_PopularDestination_Luxury.jpg';
import sneakers from '../images/categories/02_PopularDestination_Sneakers.jpg';
import pna from '../images/categories/03_PopularDestination_Tire.jpg';
import refurbished from '../images/categories/ECM_PopularDestination_Reburbished.jpg';
import tradingCards from '../images/categories/05_PopularDestination_Cards.jpg';
import preLovedLuxury from '../images/categories/06_PopularDestination_PreLoved.jpg';
import toys from '../images/categories/07_PopularDestination_Toys.jpg';
import other from '../images/categories/other.png';
import { useNavigate } from 'react-router-dom';

type Category= {
  
  name: string;
    img: string;
  
}

const categories = [
    {  name: 'Luxury', img: luxury },
    {  name: 'Sneakers', img: sneakers },
    { name: 'P&A', img: pna},
    {  name: 'Refurbished', img: refurbished},
    {  name: 'Trading Cards', img:  tradingCards },
    {  name: 'Pre Loved Luxury', img: preLovedLuxury },
    {  name: 'Toys', img: toys },
    {  name: 'Other', img: other},
  ];

type CategoryCarouselProps= {
  categories: Category[];
}

const CategoriesBar = () => {
    const [carouselRef, setcarouselRef]= useState(0);
    const [currentCategory, setCategory]= useState(categories.slice(0,5));
    const navigate = useNavigate();
    const scrollLeft = () => {
        if(carouselRef>0){
            setcarouselRef(carouselRef-1);
        setCategory(categories.slice(carouselRef-1,carouselRef+4));
        }
    };
  
    const scrollRight = () => {
        if(carouselRef<categories.length-5){
            setcarouselRef(carouselRef+1);
            setCategory(categories.slice(carouselRef+1,carouselRef+6));
        }
    };
    const handleClick = (category: Category) => {
        navigate(`/search-results?category=${category.name}&minPrice=0&maxPrice=1000`);
    }
    return (
      <div className="product-carousel">
        <button className="arrow left" onClick={scrollLeft}>&lt;</button>
        <button className="arrow right" onClick={scrollRight}>&gt;</button>
        <div className="product-list"  >
          {currentCategory.map((category, index) => (
            <div className="product-item" key={index} onClick={() => handleClick(category)}>
              <h3>{category.name}</h3>
                <img src={category.img} alt="Category"></img>
              
              
            </div>
          ))}
        </div>
        
      </div>
    );
  };
  
export default CategoriesBar;


