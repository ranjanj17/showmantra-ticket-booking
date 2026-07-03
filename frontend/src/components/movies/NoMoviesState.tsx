import React from 'react';
import { Film } from 'lucide-react';

interface NoMoviesStateProps {
  city: string;
}

export const NoMoviesState: React.FC<NoMoviesStateProps> = ({ city }) => {
  return (
    <div className="w-full bg-white border-2 border-dashed border-gray-200 min-h-[60vh] flex flex-col items-center justify-center text-center mb-10 rounded-3xl shadow-sm p-8">
      <div className="w-24 h-24 bg-red-50 rounded-full flex items-center justify-center mb-6 shadow-inner">
        <Film size={40} className="text-red-400" />
      </div>
      <h2 className="text-3xl font-extrabold text-gray-900 mb-3 tracking-tight">
        No Movies Playing in {city}
      </h2>
      <p className="text-gray-500 text-lg max-w-md">
        We couldn't find any active theaters or scheduled shows in your area right now. Try selecting a different city like Mumbai or Delhi-NCR from the top menu!
      </p>
    </div>
  );
};
