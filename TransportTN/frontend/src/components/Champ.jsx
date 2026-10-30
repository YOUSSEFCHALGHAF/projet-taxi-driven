import { useId } from 'react';

/**
 * Champ de formulaire avec libellé, aide et message d'erreur reliés pour les lecteurs d'écran.
 * `as="select"` pour une liste déroulante.
 */
export default function Champ({ label, erreur, aide, as: Element = 'input', className = '', children, ...props }) {
  const id = useId();
  const idMessage = `${id}-message`;
  return (
    <div className={className}>
      <label htmlFor={id} className="mb-1.5 block text-sm font-bold">{label}</label>
      <Element
        id={id}
        className={`champ ${erreur ? 'champ-invalide' : ''}`}
        aria-invalid={erreur ? 'true' : undefined}
        aria-describedby={erreur || aide ? idMessage : undefined}
        {...props}
      >
        {children}
      </Element>
      {(erreur || aide) && (
        <p id={idMessage} className={`mt-1 text-sm ${erreur ? 'font-semibold text-louage' : 'text-encre-doux'}`}>
          {erreur ?? aide}
        </p>
      )}
    </div>
  );
}