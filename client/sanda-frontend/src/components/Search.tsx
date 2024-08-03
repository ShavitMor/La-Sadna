import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {Categories} from '../models/CategoriesConst'

import '../styles/search.css';


const SearchBar = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [searchCategory, setSearchCategory] = useState('all');
  const [minPrice, setMinPrice] = useState(0); // Default price
const [maxPrice, setMaxPrice] = useState(10000000); // Default price
  const navigate = useNavigate();


  const handleInputChange = (event:any) => {
    setSearchTerm(event.target.value);
  };

  const handleSearchSubmit = (event:any) => {
    event.preventDefault();
    // Add your search logic here
  navigate(`/search-results?term=${encodeURIComponent(searchTerm)}&category=${encodeURIComponent(searchCategory)}&minPrice=${minPrice}&maxPrice=${maxPrice}`);
  };
  const handleCategoryChange = (event:any) => {
    setSearchCategory(event.target.value);
    // Add your search logic herex  
  };

    const handleMinPriceChange = (event:any) =>{
       setMinPrice(event.target.value);
    };
    const handleMaxPriceChange = (event:any) =>{
        setMaxPrice(event.target.value);
     };
  return (
    <nav className="searcharea">
        <select className="category"  // Add this line
            value={searchCategory} onChange={handleCategoryChange}>
            {Categories.map((category:string, index:number) => (
          <option key={index} value={category}>{category}</option>
        ))}
          </select>
        <input className='search-input'
          type="text"
          placeholder="Search Product..."
          value={searchTerm}
          onChange={handleInputChange}
        />
        <button type="submit" onClick={handleSearchSubmit}>Search</button>
        
          <div className="price-range">
            <label>
              Min Price: ${minPrice}
              <input
                type="range"
                min="0"
                step="50"
                max={maxPrice}
                value={minPrice}
                onChange={handleMinPriceChange}
              />
            </label>
            <label>
              Max Price: ${maxPrice}
              <input
                type="range"
                min={minPrice}
                step="50"
                max="10000000"
                value={maxPrice}
                onChange={handleMaxPriceChange}
              />
            </label>
          </div>        
    </nav>
  );
};

export default SearchBar;
